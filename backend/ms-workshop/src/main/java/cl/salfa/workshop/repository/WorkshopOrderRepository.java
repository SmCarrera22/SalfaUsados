package cl.salfa.workshop.repository;

import cl.salfa.workshop.entity.WorkshopOrder;
import cl.salfa.workshop.entity.WorkshopOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkshopOrderRepository
        extends JpaRepository<WorkshopOrder, Long> {

    List<WorkshopOrder> findByVehicleIdOrderByEntryDateDescIdDesc(
            Long vehicleId
    );

    boolean existsByVehicleIdAndStatusNot(
            Long vehicleId,
            WorkshopOrderStatus status
    );
}