package cl.salfa.vehicles.messaging.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VehicleOutboxRepository
        extends JpaRepository<VehicleOutboxEvent, UUID> {
}
