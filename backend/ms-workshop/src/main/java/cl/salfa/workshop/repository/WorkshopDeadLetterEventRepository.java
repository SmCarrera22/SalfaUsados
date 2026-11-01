package cl.salfa.workshop.repository;

import cl.salfa.workshop.entity.WorkshopDeadLetterEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkshopDeadLetterEventRepository
        extends JpaRepository<WorkshopDeadLetterEvent, Long> {
}