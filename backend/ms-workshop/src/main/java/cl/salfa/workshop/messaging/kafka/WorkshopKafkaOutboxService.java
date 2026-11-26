package cl.salfa.workshop.messaging.kafka;

import cl.salfa.workshop.dto.WorkshopOrderResponse;
import cl.salfa.workshop.messaging.dto.WorkshopOrderCreatedEvent;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkshopKafkaOutboxService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Value("${salfa.workshop.outbox.enabled:false}")
    private boolean enabled;

    // Debe ejecutarse dentro de la transacción que
    // confirma la creación de la orden.
    public void registerCreated(WorkshopOrderResponse order) {

        if (!enabled) {
            return;
        }

        if (order == null
                || order.id() == null
                || order.vehicleId() == null
                || order.workshopId() == null) {
            throw new IllegalArgumentException(
                    "Orden inválida para registrar Outbox"
            );
        }

        UUID eventId = UUID.randomUUID();

        WorkshopOrderCreatedEvent event =
                new WorkshopOrderCreatedEvent(
                        eventId,
                        order.id(),
                        order.vehicleId(),
                        order.workshopId(),
                        "WORKSHOP_ORDER_CREATED",
                        Instant.now()
                );

        String payload =
                objectMapper.writeValueAsString(event);

        jdbcTemplate.update(
                """
                INSERT INTO workshop_kafka_outbox (
                    event_id,
                    order_id,
                    event_key,
                    payload,
                    status,
                    attempts,
                    next_attempt_at
                )
                VALUES (?, ?, ?, ?, 'PENDING', 0, CURRENT_TIMESTAMP)
                """,
                eventId,
                order.id(),
                order.vehicleId().toString(),
                payload
        );
    }
}
