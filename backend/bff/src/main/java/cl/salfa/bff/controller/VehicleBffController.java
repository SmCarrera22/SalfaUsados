package cl.salfa.bff.controller;

import cl.salfa.bff.dto.VehicleRequest;
import cl.salfa.bff.dto.VehicleResponse;
import cl.salfa.bff.service.VehicleBffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleBffController {

    private final VehicleBffService vehicleService;

    @GetMapping
    public ResponseEntity<List<VehicleResponse>> findAll() {
        return ResponseEntity.ok(
                vehicleService.findAll()
        );
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
            @RequestBody VehicleRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(vehicleService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponse> update(
            @PathVariable Long id,
            @RequestBody VehicleRequest request
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