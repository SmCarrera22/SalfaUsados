package cl.salfa.vehicles.controller;

import cl.salfa.vehicles.dto.VehicleRequest;
import cl.salfa.vehicles.dto.VehicleResponse;
import cl.salfa.vehicles.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    public ResponseEntity<List<VehicleResponse>> findAll() {
        return ResponseEntity.ok(vehicleService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponse> findById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                vehicleService.findById(id)
        );
    }

    @PostMapping
    public ResponseEntity<VehicleResponse> create(
            @Valid @RequestBody VehicleRequest request
    ) {
        VehicleResponse response = vehicleService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody VehicleRequest request
    ) {
        return ResponseEntity.ok(
                vehicleService.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        vehicleService.delete(id);

        return ResponseEntity.noContent().build();
    }
}