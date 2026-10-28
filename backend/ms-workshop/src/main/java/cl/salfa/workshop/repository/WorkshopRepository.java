package cl.salfa.workshop.repository;

import cl.salfa.workshop.entity.Workshop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkshopRepository extends JpaRepository<Workshop, Long> {

    List<Workshop> findByActiveTrueOrderByNameAsc();
}