package cl.salfa.bff.controller;

import cl.salfa.bff.service.WorkshopBffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class WorkshopBffController {

    private final WorkshopBffService service;

    // =========================
    // TALLERES
    // =========================

    @GetMapping("/api/workshops")
    public ResponseEntity<String> findWorkshops() {
        return service.get("/api/workshops");
    }

    @GetMapping("/api/workshops/{id}")
    public ResponseEntity<String> findWorkshop(
            @PathVariable Long id
    ) {
        return service.get("/api/workshops/" + id);
    }

    @PostMapping("/api/workshops")
    public ResponseEntity<String> createWorkshop(
            @RequestBody String body
    ) {
        return service.send(
                HttpMethod.POST,
                "/api/workshops",
                body
        );
    }

    // =========================
    // ÓRDENES DE TALLER
    // =========================

    @PostMapping("/api/workshop-orders")
    public ResponseEntity<String> createOrder(
            @RequestBody String body
    ) {
        return service.send(
                HttpMethod.POST,
                "/api/workshop-orders",
                body
        );
    }

    @GetMapping("/api/workshop-orders/{id}")
    public ResponseEntity<String> findOrder(
            @PathVariable Long id
    ) {
        return service.get("/api/workshop-orders/" + id);
    }

    @GetMapping("/api/workshop-orders/vehicle/{vehicleId}")
    public ResponseEntity<String> findOrdersByVehicle(
            @PathVariable Long vehicleId
    ) {
        return service.get(
                "/api/workshop-orders/vehicle/" + vehicleId
        );
    }

    @PutMapping("/api/workshop-orders/{id}")
    public ResponseEntity<String> updateOrder(
            @PathVariable Long id,
            @RequestBody String body
    ) {
        return service.send(
                HttpMethod.PUT,
                "/api/workshop-orders/" + id,
                body
        );
    }

    @PostMapping("/api/workshop-orders/{id}/close")
    public ResponseEntity<String> closeOrder(
            @PathVariable Long id,
            @RequestBody String body
    ) {
        return service.send(
                HttpMethod.POST,
                "/api/workshop-orders/" + id + "/close",
                body
        );
    }

    @PostMapping("/api/workshop-orders/{id}/transfer")
    public ResponseEntity<String> transferOrder(
            @PathVariable Long id,
            @RequestBody String body
    ) {
        return service.send(
                HttpMethod.POST,
                "/api/workshop-orders/" + id + "/transfer",
                body
        );
    }

    @PostMapping("/api/workshop-orders/commands")
    public ResponseEntity<String> createOrderAsync(
            @RequestBody String body
    ) {
        return service.send(
                HttpMethod.POST,
                "/api/workshop-orders/commands",
                body
        );
    }
}