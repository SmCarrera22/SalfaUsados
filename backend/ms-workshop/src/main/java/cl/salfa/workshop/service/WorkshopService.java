package cl.salfa.workshop.service;

import cl.salfa.workshop.dto.WorkshopRequest;
import cl.salfa.workshop.entity.Workshop;
import cl.salfa.workshop.repository.WorkshopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cl.salfa.workshop.dto.WorkshopMetricsResponse;
import cl.salfa.workshop.entity.WorkshopOrderStatus;
import cl.salfa.workshop.repository.WorkshopOrderRepository;

import java.math.BigDecimal;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class WorkshopService {

    private final WorkshopRepository workshopRepository;
    private final WorkshopOrderRepository workshopOrderRepository;

    @Transactional
    public Workshop create(WorkshopRequest request) {

        Workshop workshop = Workshop.builder()
                .name(request.name().trim())
                .city(request.city())
                .address(request.address())
                .active(true)
                .build();

        return workshopRepository.save(workshop);
    }

    @Transactional(readOnly = true)
    public List<Workshop> findAll() {
        return workshopRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Workshop findById(Long id) {
        return workshopRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Taller no encontrado: " + id
                ));
    }

    @Transactional(readOnly = true)
    public WorkshopMetricsResponse findMetrics(Long workshopId) {

        // Reutilizamos la consulta existente para verificar
        // que el taller realmente exista.
        Workshop workshop = findById(workshopId);

        long openOrders =
                workshopOrderRepository.countByWorkshop_IdAndStatus(
                        workshopId,
                        WorkshopOrderStatus.OPEN
                );

        long inProgressOrders =
                workshopOrderRepository.countByWorkshop_IdAndStatus(
                        workshopId,
                        WorkshopOrderStatus.IN_PROGRESS
                );

        long closedOrders =
                workshopOrderRepository.countByWorkshop_IdAndStatus(
                        workshopId,
                        WorkshopOrderStatus.CLOSED
                );

        BigDecimal approvedBudgetTotal =
                workshopOrderRepository.sumApprovedBudgetByWorkshopId(
                        workshopId
                );

        BigDecimal finalCostTotal =
                workshopOrderRepository.sumFinalCostByWorkshopId(
                        workshopId
                );

        return new WorkshopMetricsResponse(
                workshop.getId(),
                workshop.getName(),
                openOrders,
                inProgressOrders,
                closedOrders,
                approvedBudgetTotal != null
                        ? approvedBudgetTotal
                        : BigDecimal.ZERO,
                finalCostTotal != null
                        ? finalCostTotal
                        : BigDecimal.ZERO
        );
    }

}