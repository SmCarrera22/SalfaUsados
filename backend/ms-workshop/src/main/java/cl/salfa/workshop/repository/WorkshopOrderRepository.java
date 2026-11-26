package cl.salfa.workshop.repository;

import cl.salfa.workshop.entity.WorkshopOrder;
import cl.salfa.workshop.entity.WorkshopOrderStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface WorkshopOrderRepository
        extends JpaRepository<WorkshopOrder, Long> {

    // Consultas existentes.
    List<WorkshopOrder> findByVehicleIdOrderByEntryDateDescIdDesc(
            Long vehicleId
    );

    boolean existsByVehicleIdAndStatusNot(
            Long vehicleId,
            WorkshopOrderStatus status
    );

    // Cantidad de órdenes por taller y estado.
    long countByWorkshop_IdAndStatus(
            Long workshopId,
            WorkshopOrderStatus status
    );

    // Suma de presupuestos aprobados registrados.
    @Query("""
            SELECT SUM(o.approvedBudget)
            FROM WorkshopOrder o
            WHERE o.workshop.id = :workshopId
            """)
    BigDecimal sumApprovedBudgetByWorkshopId(
            @Param("workshopId") Long workshopId
    );

    // Suma de costos finales registrados.
    @Query("""
            SELECT SUM(o.finalCost)
            FROM WorkshopOrder o
            WHERE o.workshop.id = :workshopId
            """)
    BigDecimal sumFinalCostByWorkshopId(
            @Param("workshopId") Long workshopId
    );
}
