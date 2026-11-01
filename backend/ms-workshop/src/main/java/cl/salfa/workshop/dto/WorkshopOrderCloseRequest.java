package cl.salfa.workshop.dto;

import cl.salfa.workshop.entity.WorkshopClosureReason;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record WorkshopOrderCloseRequest(

        @NotNull
        LocalDate actualExitDate,

        @NotNull
        WorkshopClosureReason closureReason,

        @DecimalMin("0.00")
        BigDecimal finalCost

) {}