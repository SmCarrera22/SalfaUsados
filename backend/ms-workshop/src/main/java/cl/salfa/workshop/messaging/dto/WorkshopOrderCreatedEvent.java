package cl.salfa.workshop.messaging.dto;

import java.time.Instant;
import java.util.UUID;

public record WorkshopOrderCreatedEvent(
        UUID eventId,
        Long orderId,
        Long vehicleId,
        Long workshopId,
        String eventType,
        Instant occurredAt
) {}