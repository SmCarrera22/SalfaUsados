package cl.salfa.workshop.dto;

import cl.salfa.workshop.entity.WorkshopClosureReason;
import cl.salfa.workshop.entity.WorkshopOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record WorkshopOrderResponse(

        Long id,
        Long vehicleId,
        Long workshopId,
        String workshopName,
        LocalDate entryDate,
        LocalDate estimatedExitDate,
        LocalDate actualExitDate,
        BigDecimal approvedBudget,
        BigDecimal finalCost,
        String description,
        WorkshopOrderStatus status,
        WorkshopClosureReason closureReason

) {}