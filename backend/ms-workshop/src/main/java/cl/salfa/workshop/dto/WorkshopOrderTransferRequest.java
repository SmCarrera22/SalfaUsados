package cl.salfa.workshop.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record WorkshopOrderTransferRequest(

        @NotNull
        @Positive
        Long destinationWorkshopId,

        @NotNull
        LocalDate transferDate,

        String description

) {}