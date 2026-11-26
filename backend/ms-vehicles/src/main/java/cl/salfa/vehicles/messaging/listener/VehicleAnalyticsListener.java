package cl.salfa.vehicles.messaging.listener;

import cl.salfa.vehicles.messaging.dto.VehicleEvent;
import cl.salfa.vehicles.service.VehicleAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "salfa.kafka.enabled",
        havingValue = "true"
)
@ConditionalOnProperty(
        name = "salfa.kafka.analytics.enabled",
        havingValue = "true"
)
public class VehicleAnalyticsListener {

    private static final Logger log =
            LoggerFactory.getLogger(
                    VehicleAnalyticsListener.class
            );

    private static final Set<String> SUPPORTED_EVENTS =
            Set.of(
                    "VEHICLE_CREATED",
                    "VEHICLE_UPDATED",
                    "VEHICLE_DELETED"
            );

    private final ObjectMapper objectMapper;
    private final VehicleAnalyticsService analyticsService;

    @KafkaListener(
            id = "vehicle-analytics-listener",
            topics = "${salfa.kafka.topics.vehicles}",
            groupId = "${salfa.kafka.groups.analytics}"
    )
    public void consume(
            ConsumerRecord<String, String> record,
            Acknowledgment acknowledgment
    ) {

        try {

            VehicleEvent event =
                    objectMapper.readValue(
                            record.value(),
                            VehicleEvent.class
                    );

            validate(event, record.key());

            boolean inserted =
                    analyticsService.register(event);

            if (inserted) {

                log.info(
                        "Analitica registrada: eventId={}, " +
                                "type={}, branch={}, partition={}, offset={}",
                        event.eventId(),
                        event.eventType(),
                        event.branch(),
                        record.partition(),
                        record.offset()
                );

            } else {

                log.info(
                        "Evento analitico duplicado ignorado: {}",
                        event.eventId()
                );
            }

            acknowledgment.acknowledge();

        } catch (Exception ex) {

            log.error(
                    "Error analitica Kafka: topic={}, " +
                            "partition={}, offset={}",
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    ex
            );

            throw new IllegalStateException(
                    "No fue posible procesar analitica",
                    ex
            );
        }
    }

    private void validate(
            VehicleEvent event,
            String key
    ) {

        if (event == null
                || event.eventId() == null
                || event.eventType() == null
                || !SUPPORTED_EVENTS.contains(event.eventType())
                || event.eventVersion() == null
                || event.eventVersion() != 1
                || event.vehicleId() == null
                || event.vin() == null
                || event.vin().length() != 17
                || event.occurredAt() == null
                || !event.vin().equals(key)) {

            throw new IllegalArgumentException(
                    "Evento invalido para analitica"
            );
        }
    }
}
