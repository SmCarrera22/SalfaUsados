package cl.salfa.vehicles.messaging.outbox;

import java.util.UUID;

public record OutboxClaim(
        UUID eventId,
        UUID claimToken,
        String eventKey,
        String payload,
        int attempt
) {
}
