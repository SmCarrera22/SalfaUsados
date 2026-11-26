package cl.salfa.vehicles.messaging.dto;

import java.time.Instant;
import java.util.UUID;

public record VehicleEvent(
        UUID eventId,
        String eventType,
        Integer eventVersion,
        Long vehicleId,
        String vin,
        String branch,
        Instant occurredAt
) {}
