package cl.salfa.bff.dto;

public record VehicleRequest(
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
        String status
) {
}