package cl.salfa.vehicles.dto;

import cl.salfa.vehicles.entity.VehicleStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VehicleRequest(

        @NotBlank(message = "El VIN es obligatorio")
        @Size(min = 17, max = 17, message = "El VIN debe contener 17 caracteres")
        String vin,

        @NotBlank(message = "La patente es obligatoria")
        @Size(max = 10, message = "La patente no puede superar los 10 caracteres")
        String plate,

        @NotBlank(message = "La marca es obligatoria")
        String brand,

        @NotBlank(message = "El modelo es obligatorio")
        String model,

        String version,

        @NotNull(message = "El año es obligatorio")
        @Min(value = 1900, message = "El año del vehículo no es válido")
        @Max(value = 2100, message = "El año del vehículo no es válido")
        Integer year,

        @Min(value = 0, message = "El kilometraje no puede ser negativo")
        Integer mileage,

        String color,

        String fuelType,

        @NotBlank(message = "La sucursal es obligatoria")
        String branch,

        @NotNull(message = "El estado es obligatorio")
        VehicleStatus status
) {
}