package cl.salfa.bff.dto;

import java.time.LocalDateTime;

public record VehicleResponse(
        Long id,
        String vin,
        String plate,
        String brand,
        String model,
        String version,
        Integer year,
        Integer mileage,
        String color,
        String fuelType,
        String branch,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}