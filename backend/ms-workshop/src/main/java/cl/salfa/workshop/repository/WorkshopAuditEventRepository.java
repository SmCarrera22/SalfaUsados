package cl.salfa.workshop.repository;

import cl.salfa.workshop.entity.WorkshopAuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WorkshopAuditEventRepository
        extends JpaRepository<WorkshopAuditEvent, Long> {

    boolean existsByEventId(UUID eventId);
}