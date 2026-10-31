package cl.salfa.workshop.service;

import cl.salfa.workshop.entity.WorkshopAuditEvent;
import cl.salfa.workshop.messaging.dto.WorkshopOrderCreatedEvent;
import cl.salfa.workshop.repository.WorkshopAuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class WorkshopAuditService {

    private final WorkshopAuditEventRepository repository;

    @Transactional
    public void register(WorkshopOrderCreatedEvent event) {

        if (event == null
                || event.eventId() == null
                || event.orderId() == null
                || event.vehicleId() == null
                || event.workshopId() == null
                || event.eventType() == null
                || event.eventType().isBlank()
                || event.occurredAt() == null) {
            throw new IllegalArgumentException(
                    "Evento de auditoría inválido"
            );
        }

        if (repository.existsByEventId(event.eventId())) {
            return;
        }

        WorkshopAuditEvent entity = new WorkshopAuditEvent();
        entity.setEventId(event.eventId());
        entity.setOrderId(event.orderId());
        entity.setVehicleId(event.vehicleId());
        entity.setWorkshopId(event.workshopId());
        entity.setEventType(event.eventType());
        entity.setOccurredAt(event.occurredAt());
        entity.setProcessedAt(Instant.now());

        repository.saveAndFlush(entity);
    }
}