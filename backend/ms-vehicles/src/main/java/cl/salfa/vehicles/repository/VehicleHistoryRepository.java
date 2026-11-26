package cl.salfa.vehicles.repository;

import cl.salfa.vehicles.entity.VehicleHistoryEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VehicleHistoryRepository
        extends JpaRepository<VehicleHistoryEvent, UUID> {

    List<VehicleHistoryEvent>
    findByVinOrderByOccurredAtDescEventIdDesc(String vin);
}
