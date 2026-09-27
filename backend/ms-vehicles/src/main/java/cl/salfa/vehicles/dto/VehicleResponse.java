package cl.salfa.vehicles.dto;

import cl.salfa.vehicles.entity.VehicleStatus;

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
        VehicleStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}