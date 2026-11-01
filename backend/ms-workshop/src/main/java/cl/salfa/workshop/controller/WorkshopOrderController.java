package cl.salfa.workshop.controller;

import cl.salfa.workshop.dto.*;
import cl.salfa.workshop.messaging.dto.CreateWorkshopOrderCommand;
import cl.salfa.workshop.messaging.publisher.WorkshopCommandPublisher;
import cl.salfa.workshop.service.WorkshopOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workshop-orders")
@RequiredArgsConstructor
public class WorkshopOrderController {

    private final WorkshopOrderService orderService;
    private final WorkshopCommandPublisher commandPublisher;

    @PostMapping
    public ResponseEntity<WorkshopOrderResponse> create(
            @Valid @RequestBody WorkshopOrderRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.create(request));
    }

    @PostMapping("/commands")
    public ResponseEntity<AcceptedCommandResponse> createAsync(
            @Valid @RequestBody WorkshopOrderRequest request
    ) {
        UUID commandId = UUID.randomUUID();

        CreateWorkshopOrderCommand command =
                new CreateWorkshopOrderCommand(
                        commandId,
                        request.vehicleId(),
                        request.workshopId(),
                        request.entryDate(),
                        request.estimatedExitDate(),
                        request.description()
                );

        commandPublisher.publish(command);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(new AcceptedCommandResponse(
                        commandId,
                        "Comando enviado para procesamiento"
                ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkshopOrderResponse> findById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<WorkshopOrderResponse>> findByVehicle(
            @PathVariable Long vehicleId
    ) {
        return ResponseEntity.ok(orderService.findByVehicle(vehicleId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkshopOrderResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody WorkshopOrderUpdateRequest request
    ) {
        return ResponseEntity.ok(orderService.update(id, request));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<WorkshopOrderResponse> close(
            @PathVariable Long id,
            @Valid @RequestBody WorkshopOrderCloseRequest request
    ) {
        return ResponseEntity.ok(orderService.close(id, request));
    }

    @PostMapping("/{id}/transfer")
    public ResponseEntity<WorkshopOrderResponse> transfer(
            @PathVariable Long id,
            @Valid @RequestBody WorkshopOrderTransferRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.transfer(id, request));
    }

    public record AcceptedCommandResponse(
            UUID commandId,
            String message
    ) {}
}