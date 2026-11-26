package cl.salfa.vehicles.messaging.outbox;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VehicleOutboxClaimService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LEASE_SECONDS = 60L;

    private final VehicleOutboxRepository repository;

    @Transactional
    public Optional<OutboxClaim> claimNext() {

        Instant now = Instant.now();

        List<VehicleOutboxEvent> candidates =
                repository.findClaimable(
                        OutboxStatus.PENDING,
                        OutboxStatus.PROCESSING,
                        now,
                        PageRequest.of(0, 1)
                );

        if (candidates.isEmpty()) {
            return Optional.empty();
        }

        VehicleOutboxEvent event = candidates.get(0);

        if (event.getAttempts() >= MAX_ATTEMPTS) {
            event.markFailed(
                    "Máximo de intentos alcanzado"
            );
            return Optional.empty();
        }

        UUID token = UUID.randomUUID();

        event.claim(
                token,
                now.plusSeconds(LEASE_SECONDS)
        );

        return Optional.of(
                new OutboxClaim(
                        event.getId(),
                        token,
                        event.getEventKey(),
                        event.getPayload(),
                        event.getAttempts()
                )
        );
    }

    @Transactional
    public boolean markPublished(OutboxClaim claim) {

        Optional<VehicleOutboxEvent> result =
                repository.findLockedById(claim.eventId());

        if (result.isEmpty()) {
            return false;
        }

        VehicleOutboxEvent event = result.get();

        if (!event.isOwnedBy(claim.claimToken())) {
            return false;
        }

        event.markPublished(Instant.now());
        return true;
    }

    @Transactional
    public boolean markRetry(
            OutboxClaim claim,
            String error
    ) {

        Optional<VehicleOutboxEvent> result =
                repository.findLockedById(claim.eventId());

        if (result.isEmpty()) {
            return false;
        }

        VehicleOutboxEvent event = result.get();

        if (!event.isOwnedBy(claim.claimToken())) {
            return false;
        }

        String safeError = error == null
                ? "Error desconocido"
                : error.substring(
                0,
                Math.min(error.length(), 1000)
        );

        if (event.getAttempts() >= MAX_ATTEMPTS) {
            event.markFailed(safeError);
            return true;
        }

        long delay = Math.min(
                300L,
                5L * (1L << (event.getAttempts() - 1))
        );

        event.scheduleRetry(
                Instant.now().plusSeconds(delay),
                safeError
        );

        return true;
    }
}
