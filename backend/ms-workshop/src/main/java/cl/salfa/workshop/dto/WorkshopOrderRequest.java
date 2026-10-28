package cl.salfa.workshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record WorkshopOrderRequest(

        @NotNull
        @Positive
        Long vehicleId,

        @NotNull
        @Positive
        Long workshopId,

        @NotNull
        LocalDate entryDate,

        LocalDate estimatedExitDate,

        @NotBlank
        String description

) {}