package cl.salfa.vehicles.controller;

import cl.salfa.vehicles.entity.VehicleHistoryEvent;
import cl.salfa.vehicles.service.VehicleHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vehicles/vin")
@RequiredArgsConstructor
public class VehicleHistoryController {

    private final VehicleHistoryService historyService;

    @GetMapping("/{vin}/history")
    public ResponseEntity<List<HistoryResponse>> findHistory(
            @PathVariable String vin
    ) {

        List<HistoryResponse> history =
                historyService.findByVin(vin)
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(history);
    }

    private HistoryResponse toResponse(
            VehicleHistoryEvent event
    ) {
        return new HistoryResponse(
                event.getEventId(),
                event.getVehicleId(),
                event.getVin(),
                event.getEventType(),
                event.getBranch(),
                event.getOccurredAt()
        );
    }

    public record HistoryResponse(
            UUID eventId,
            Long vehicleId,
            String vin,
            String eventType,
            String branch,
            Instant occurredAt
    ) {}
}
