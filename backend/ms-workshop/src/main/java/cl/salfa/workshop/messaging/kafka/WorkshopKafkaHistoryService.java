package cl.salfa.workshop.messaging.kafka;

import cl.salfa.workshop.messaging.dto.WorkshopOrderCreatedEvent;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkshopKafkaHistoryService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public boolean register(
            WorkshopOrderCreatedEvent event
    ) {
        int inserted = jdbcTemplate.update(
                """
                INSERT INTO workshop_kafka_consumed_events (
                    event_id,
                    order_id,
                    vehicle_id,
                    workshop_id,
                    event_type,
                    occurred_at
                )
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT (event_id) DO NOTHING
                """,
                event.eventId(),
                event.orderId(),
                event.vehicleId(),
                event.workshopId(),
                event.eventType(),
                java.sql.Timestamp.from(
                        event.occurredAt()
                )
        );

        return inserted == 1;
    }
}
