package cl.salfa.workshop.messaging.kafka;

import cl.salfa.workshop.messaging.dto.WorkshopOrderCreatedEvent;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "salfa.kafka.enabled",
        havingValue = "true"
)
public class WorkshopKafkaEventListener {

    private static final Logger log =
            LoggerFactory.getLogger(
                    WorkshopKafkaEventListener.class
            );

    private final ObjectMapper objectMapper;
    private final WorkshopKafkaHistoryService historyService;

    @KafkaListener(
            topics = "${salfa.kafka.topics.workshops}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(
            ConsumerRecord<String, String> record,
            Acknowledgment acknowledgment
    ) {

        WorkshopOrderCreatedEvent event =
                objectMapper.readValue(
                        record.value(),
                        WorkshopOrderCreatedEvent.class
                );

        if (event == null
                || event.eventId() == null
                || event.orderId() == null
                || event.vehicleId() == null
                || event.workshopId() == null
                || event.occurredAt() == null
                || !"WORKSHOP_ORDER_CREATED".equals(
                event.eventType()
        )
                || !event.vehicleId().toString().equals(
                record.key()
        )) {
            throw new IllegalArgumentException(
                    "Evento Kafka de taller inválido"
            );
        }

        boolean inserted = historyService.register(event);

        if (inserted) {
            log.info(
                    "Evento Kafka workshop registrado: eventId={}, orderId={}",
                    event.eventId(),
                    event.orderId()
            );
        } else {
            log.info(
                    "Evento Kafka workshop duplicado ignorado: {}",
                    event.eventId()
            );
        }

        acknowledgment.acknowledge();
    }
}
