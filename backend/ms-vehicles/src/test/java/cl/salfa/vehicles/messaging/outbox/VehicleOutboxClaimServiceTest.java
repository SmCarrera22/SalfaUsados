package cl.salfa.vehicles.messaging.outbox;

import cl.salfa.vehicles.messaging.dto.VehicleEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class VehicleOutboxClaimServiceTest {

    @Autowired
    private VehicleOutboxService outboxService;

    @Autowired
    private VehicleOutboxClaimService claimService;

    @Autowired
    private VehicleOutboxRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    private UUID createEvent() {
        UUID id = UUID.randomUUID();

        outboxService.store(
                new VehicleEvent(
                        id,
                        "VEHICLE_CREATED",
                        1,
                        100L,
                        "TESTVIN0000000001",
                        "SANTIAGO",
                        Instant.now()
                )
        );

        return id;
    }

    @Test
    void shouldClaimPendingEvent() {
        UUID id = createEvent();

        OutboxClaim claim =
                claimService.claimNext().orElseThrow();

        assertEquals(id, claim.eventId());
        assertEquals(1, claim.attempt());

        var persisted =
                repository.findById(id).orElseThrow();

        assertEquals(
                OutboxStatus.PROCESSING,
                persisted.getStatus()
        );
        assertNotNull(persisted.getClaimToken());

        assertTrue(claimService.claimNext().isEmpty());
    }

    @Test
    void shouldMarkEventPublished() {
        UUID id = createEvent();

        OutboxClaim claim =
                claimService.claimNext().orElseThrow();

        assertTrue(claimService.markPublished(claim));

        var persisted =
                repository.findById(id).orElseThrow();

        assertEquals(
                OutboxStatus.PUBLISHED,
                persisted.getStatus()
        );
        assertNotNull(persisted.getPublishedAt());
        assertNull(persisted.getClaimToken());
    }

    @Test
    void shouldScheduleRetry() {
        UUID id = createEvent();

        OutboxClaim claim =
                claimService.claimNext().orElseThrow();

        assertTrue(
                claimService.markRetry(
                        claim,
                        "Kafka no disponible"
                )
        );

        var persisted =
                repository.findById(id).orElseThrow();

        assertEquals(
                OutboxStatus.PENDING,
                persisted.getStatus()
        );
        assertEquals(1, persisted.getAttempts());
        assertTrue(
                persisted.getNextAttemptAt()
                        .isAfter(Instant.now())
        );
        assertTrue(claimService.claimNext().isEmpty());
    }

    @Test
    void shouldRejectInvalidToken() {
        UUID id = createEvent();

        OutboxClaim original =
                claimService.claimNext().orElseThrow();

        OutboxClaim invalid = new OutboxClaim(
                original.eventId(),
                UUID.randomUUID(),
                original.eventKey(),
                original.payload(),
                original.attempt()
        );

        assertFalse(claimService.markPublished(invalid));

        assertEquals(
                OutboxStatus.PROCESSING,
                repository.findById(id)
                        .orElseThrow()
                        .getStatus()
        );
    }


    @Test
    void shouldRecoverExpiredClaimWithNewToken() {

        UUID id = createEvent();

        // Primera instancia reclama el evento.
        OutboxClaim first =
                claimService.claimNext().orElseThrow();

        assertEquals(1, first.attempt());

        // Simulamos que el proceso se detuvo
        // y el lease venció.
        VehicleOutboxEvent event =
                repository.findById(id).orElseThrow();

        ReflectionTestUtils.setField(
                event,
                "claimedUntil",
                Instant.now().minusSeconds(10)
        );

        repository.saveAndFlush(event);

        // Una segunda instancia recupera el evento.
        OutboxClaim recovered =
                claimService.claimNext().orElseThrow();

        assertEquals(id, recovered.eventId());
        assertEquals(2, recovered.attempt());

        assertNotEquals(
                first.claimToken(),
                recovered.claimToken()
        );

        // El token anterior ya no puede confirmar.
        assertFalse(
                claimService.markPublished(first)
        );

        // El nuevo propietario sí puede confirmar.
        assertTrue(
                claimService.markPublished(recovered)
        );

        VehicleOutboxEvent persisted =
                repository.findById(id).orElseThrow();

        assertEquals(
                OutboxStatus.PUBLISHED,
                persisted.getStatus()
        );

        assertEquals(2, persisted.getAttempts());
    }


    @Test
    void shouldMarkFailedAfterMaximumExpiredClaims() {

        UUID id = createEvent();

        for (int attempt = 1; attempt <= 5; attempt++) {

            OutboxClaim claim =
                    claimService.claimNext().orElseThrow();

            assertEquals(attempt, claim.attempt());

            VehicleOutboxEvent event =
                    repository.findById(id).orElseThrow();

            // Simulamos una caída del proceso
            // y el vencimiento del lease.
            ReflectionTestUtils.setField(
                    event,
                    "claimedUntil",
                    Instant.now().minusSeconds(10)
            );

            repository.saveAndFlush(event);
        }

        // El evento ha agotado sus cinco intentos.
        assertTrue(
                claimService.claimNext().isEmpty()
        );

        VehicleOutboxEvent persisted =
                repository.findById(id).orElseThrow();

        assertEquals(
                OutboxStatus.FAILED,
                persisted.getStatus()
        );

        assertEquals(5, persisted.getAttempts());

        assertNull(persisted.getClaimToken());
        assertNull(persisted.getClaimedUntil());
    }
}
