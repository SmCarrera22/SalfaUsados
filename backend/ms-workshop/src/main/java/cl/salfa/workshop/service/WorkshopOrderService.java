package cl.salfa.workshop.service;

import cl.salfa.workshop.dto.*;
import cl.salfa.workshop.entity.*;
import cl.salfa.workshop.repository.WorkshopOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class WorkshopOrderService {

    private final WorkshopOrderRepository orderRepository;
    private final WorkshopService workshopService;

    @Transactional
    public WorkshopOrderResponse create(WorkshopOrderRequest request) {

        validateNoActiveOrder(request.vehicleId());

        if (request.estimatedExitDate() != null
                && request.estimatedExitDate().isBefore(request.entryDate())) {
            throw new IllegalArgumentException(
                    "La fecha comprometida no puede ser anterior al ingreso"
            );
        }

        Workshop workshop = workshopService.findById(request.workshopId());

        if (!Boolean.TRUE.equals(workshop.getActive())) {
            throw new IllegalArgumentException(
                    "No se pueden crear órdenes para talleres inactivos"
            );
        }

        WorkshopOrder order = WorkshopOrder.builder()
                .vehicleId(request.vehicleId())
                .workshop(workshop)
                .entryDate(request.entryDate())
                .estimatedExitDate(request.estimatedExitDate())
                .description(request.description())
                .status(WorkshopOrderStatus.OPEN)
                .build();

        return toResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public WorkshopOrderResponse findById(Long id) {
        return toResponse(findEntity(id));
    }

    @Transactional(readOnly = true)
    public List<WorkshopOrderResponse> findByVehicle(Long vehicleId) {
        return orderRepository
                .findByVehicleIdOrderByEntryDateDescIdDesc(vehicleId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public WorkshopOrderResponse update(
            Long id,
            WorkshopOrderUpdateRequest request
    ) {

        WorkshopOrder order = findEntity(id);
        validateEditable(order);

        if (request.estimatedExitDate() != null
                && request.estimatedExitDate().isBefore(order.getEntryDate())) {
            throw new IllegalArgumentException(
                    "La fecha comprometida no puede ser anterior al ingreso"
            );
        }

        if (request.estimatedExitDate() != null) {
            order.setEstimatedExitDate(request.estimatedExitDate());
        }

        if (request.approvedBudget() != null) {
            order.setApprovedBudget(request.approvedBudget());
        }

        if (request.description() != null) {
            order.setDescription(request.description());
        }

        order.setStatus(WorkshopOrderStatus.IN_PROGRESS);

        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public WorkshopOrderResponse close(
            Long id,
            WorkshopOrderCloseRequest request
    ) {

        WorkshopOrder order = findEntity(id);
        validateEditable(order);

        validateCloseDate(order, request.actualExitDate());

        if (request.closureReason() == WorkshopClosureReason.TRANSFERRED) {
            throw new IllegalArgumentException(
                    "Para un traslado debe utilizar el endpoint de transferencia"
            );
        }

        order.setActualExitDate(request.actualExitDate());
        order.setClosureReason(request.closureReason());
        order.setFinalCost(request.finalCost());
        order.setStatus(WorkshopOrderStatus.CLOSED);

        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public WorkshopOrderResponse transfer(
            Long id,
            WorkshopOrderTransferRequest request
    ) {

        WorkshopOrder current = findEntity(id);
        validateEditable(current);
        validateCloseDate(current, request.transferDate());

        Workshop destination = workshopService.findById(
                request.destinationWorkshopId()
        );

        if (!Boolean.TRUE.equals(destination.getActive())) {
            throw new IllegalArgumentException(
                    "El taller de destino está inactivo"
            );
        }

        if (current.getWorkshop().getId().equals(destination.getId())) {
            throw new IllegalArgumentException(
                    "El taller de destino debe ser diferente al actual"
            );
        }

        current.setActualExitDate(request.transferDate());
        current.setClosureReason(WorkshopClosureReason.TRANSFERRED);
        current.setStatus(WorkshopOrderStatus.CLOSED);

        orderRepository.saveAndFlush(current);

        WorkshopOrder next = WorkshopOrder.builder()
                .vehicleId(current.getVehicleId())
                .workshop(destination)
                .entryDate(request.transferDate())
                .description(request.description() == null
                        ? "Ingreso por traslado desde orden " + current.getId()
                        : request.description())
                .status(WorkshopOrderStatus.OPEN)
                .build();

        return toResponse(orderRepository.save(next));
    }

    private WorkshopOrder findEntity(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Orden de taller no encontrada: " + id
                ));
    }

    private void validateNoActiveOrder(Long vehicleId) {

        if (orderRepository.existsByVehicleIdAndStatusNot(
                vehicleId,
                WorkshopOrderStatus.CLOSED
        )) {
            throw new IllegalStateException(
                    "El vehículo ya tiene una orden de taller abierta"
            );
        }
    }

    private void validateEditable(WorkshopOrder order) {

        if (order.getStatus() == WorkshopOrderStatus.CLOSED) {
            throw new IllegalStateException(
                    "No se puede modificar una orden cerrada"
            );
        }
    }

    private void validateCloseDate(
            WorkshopOrder order,
            LocalDate exitDate
    ) {
        if (exitDate.isBefore(order.getEntryDate())) {
            throw new IllegalArgumentException(
                    "La fecha de salida no puede ser anterior al ingreso"
            );
        }
    }

    private WorkshopOrderResponse toResponse(WorkshopOrder order) {

        return new WorkshopOrderResponse(
                order.getId(),
                order.getVehicleId(),
                order.getWorkshop().getId(),
                order.getWorkshop().getName(),
                order.getEntryDate(),
                order.getEstimatedExitDate(),
                order.getActualExitDate(),
                order.getApprovedBudget(),
                order.getFinalCost(),
                order.getDescription(),
                order.getStatus(),
                order.getClosureReason()
        );
    }
}