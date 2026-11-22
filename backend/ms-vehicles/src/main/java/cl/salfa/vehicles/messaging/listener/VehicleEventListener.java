package cl.salfa.vehicles.messaging.listener;

import cl.salfa.vehicles.messaging.dto.VehicleEvent;
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
public class VehicleEventListener {

    private static final Logger log =
            LoggerFactory.getLogger(VehicleEventListener.class);

    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${salfa.kafka.topics.vehicles}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(
            ConsumerRecord<String, String> record,
            Acknowledgment acknowledgment
    ) {
        try {
            VehicleEvent event = objectMapper.readValue(
                    record.value(),
                    VehicleEvent.class
            );

            validate(event, record.key());

            log.info(
                    "Evento Kafka recibido: eventId={}, " +
                            "type={}, vin={}, partition={}, offset={}",
                    event.eventId(),
                    event.eventType(),
                    event.vin(),
                    record.partition(),
                    record.offset()
            );

            // Por ahora solo validamos y observamos el evento.
            // La persistencia del historial se implementara
            // en un servicio de negocio independiente.

            acknowledgment.acknowledge();

        } catch (Exception ex) {
            log.error(
                    "Error procesando evento Kafka: " +
                            "topic={}, partition={}, offset={}",
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    ex
            );

            throw new IllegalStateException(
                    "No fue posible procesar el evento Kafka",
                    ex
            );
        }
    }

    private void validate(VehicleEvent event, String key) {

        if (event == null
                || event.eventId() == null
                || event.eventType() == null
                || event.eventType().isBlank()
                || event.eventVersion() == null
                || event.eventVersion() != 1
                || event.vehicleId() == null
                || event.vin() == null
                || event.vin().isBlank()
                || event.occurredAt() == null) {

            throw new IllegalArgumentException(
                    "Evento de vehiculo incompleto o version no soportada"
            );
        }

        if (!event.vin().equals(key)) {
            throw new IllegalArgumentException(
                    "La clave Kafka no coincide con el VIN"
            );
        }
    }
}
