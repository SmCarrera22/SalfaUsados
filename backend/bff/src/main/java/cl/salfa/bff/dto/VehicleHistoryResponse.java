package cl.salfa.bff.dto;

import java.time.Instant;
import java.util.UUID;

public record VehicleHistoryResponse(
        UUID eventId,
        Long vehicleId,
        String vin,
        String eventType,
        String branch,
        Instant occurredAt
) {}
