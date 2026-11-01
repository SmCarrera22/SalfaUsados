package cl.salfa.workshop.dto;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.time.LocalDate;

public record WorkshopOrderUpdateRequest(

        LocalDate estimatedExitDate,

        @DecimalMin("0.00")
        BigDecimal approvedBudget,

        String description

) {}