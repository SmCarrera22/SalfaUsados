package cl.salfa.vehicles.messaging.outbox;

import cl.salfa.vehicles.messaging.dto.VehicleEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class VehicleOutboxService {

    private final VehicleOutboxRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void store(VehicleEvent event) {

        Objects.requireNonNull(
                event,
                "El evento no puede ser nulo"
        );

        Objects.requireNonNull(
                event.eventId(),
                "eventId es obligatorio"
        );

        Objects.requireNonNull(
                event.vehicleId(),
                "vehicleId es obligatorio"
        );

        if (event.vin() == null ||
                event.vin().length() != 17) {

            throw new IllegalArgumentException(
                    "VIN debe tener 17 caracteres"
            );
        }

        if (event.eventVersion() == null ||
                event.eventVersion() != 1) {

            throw new IllegalArgumentException(
                    "Version del evento no soportada"
            );
        }

        if (event.eventType() == null ||
                event.eventType().isBlank()) {

            throw new IllegalArgumentException(
                    "eventType es obligatorio"
            );
        }

        Objects.requireNonNull(
                event.occurredAt(),
                "occurredAt es obligatorio"
        );

        String payload = objectMapper.writeValueAsString(event);

        VehicleOutboxEvent outbox = new VehicleOutboxEvent(
                event.eventId(),
                event.vehicleId(),
                event.eventType(),
                event.eventVersion(),
                event.vin(),
                payload
        );

        repository.save(outbox);
    }
}
