package cl.salfa.vehicles.messaging.publisher;

import cl.salfa.vehicles.messaging.dto.VehicleEvent;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "salfa.kafka.enabled",
        havingValue = "true"
)
public class VehicleEventPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(
                    VehicleEventPublisher.class
            );

    private static final String TOPIC = "vehicle.events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public CompletableFuture<SendResult<String, String>> publish(
            VehicleEvent event
    ) {

        Objects.requireNonNull(
                event,
                "El evento no puede ser nulo"
        );

        Objects.requireNonNull(
                event.eventId(),
                "eventId es obligatorio"
        );

        if (event.vin() == null || event.vin().isBlank()) {
            throw new IllegalArgumentException(
                    "VIN obligatorio"
            );
        }

        String json = objectMapper.writeValueAsString(event);

        CompletableFuture<SendResult<String, String>> future =
                kafkaTemplate.send(
                        TOPIC,
                        event.vin(),
                        json
                );

        future.whenComplete((result, exception) -> {

            if (exception != null) {
                log.error(
                        "Error publicando evento Kafka: eventId={}",
                        event.eventId(),
                        exception
                );
                return;
            }

            log.info(
                    "Evento confirmado por Kafka: " +
                            "eventId={}, partition={}, offset={}",
                    event.eventId(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset()
            );
        });

        return future;
    }
}
