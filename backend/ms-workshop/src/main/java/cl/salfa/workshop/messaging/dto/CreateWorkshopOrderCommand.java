package cl.salfa.workshop.messaging.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CreateWorkshopOrderCommand(
        UUID eventId,
        Long vehicleId,
        Long workshopId,
        LocalDate entryDate,
        LocalDate estimatedExitDate,
        String description
) {}