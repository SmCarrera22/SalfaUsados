package cl.salfa.workshop.dto;

import java.math.BigDecimal;

public record WorkshopMetricsResponse(
        Long workshopId,
        String workshopName,
        long openOrders,
        long inProgressOrders,
        long closedOrders,
        BigDecimal approvedBudgetTotal,
        BigDecimal finalCostTotal
) {}
