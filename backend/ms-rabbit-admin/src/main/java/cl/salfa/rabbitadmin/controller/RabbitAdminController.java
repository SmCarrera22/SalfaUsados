package cl.salfa.rabbitadmin.controller;

import cl.salfa.rabbitadmin.dto.CreateBindingRequest;
import cl.salfa.rabbitadmin.dto.CreateExchangeRequest;
import cl.salfa.rabbitadmin.dto.CreateQueueRequest;
import cl.salfa.rabbitadmin.service.RabbitAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/rabbit-admin")
@RequiredArgsConstructor
public class RabbitAdminController {

    private final RabbitAdminService service;

    @PostMapping("/queues")
    public ResponseEntity<Map<String, Object>> createQueue(
            @Valid @RequestBody CreateQueueRequest request
    ) {
        return ResponseEntity.status(201).body(
                service.createQueue(request)
        );
    }

    @DeleteMapping("/queues/{name}")
    public ResponseEntity<Void> deleteQueue(
            @PathVariable String name
    ) {
        service.deleteQueue(name);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/exchanges")
    public ResponseEntity<Map<String, String>> createExchange(
            @Valid @RequestBody CreateExchangeRequest request
    ) {
        return ResponseEntity.status(201).body(
                service.createExchange(request)
        );
    }

    @DeleteMapping("/exchanges/{name}")
    public ResponseEntity<Void> deleteExchange(
            @PathVariable String name
    ) {
        service.deleteExchange(name);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bindings")
    public ResponseEntity<Map<String, String>> createBinding(
            @Valid @RequestBody CreateBindingRequest request
    ) {
        return ResponseEntity.status(201).body(
                service.createBinding(request)
        );
    }

    @DeleteMapping("/bindings")
    public ResponseEntity<Void> deleteBinding(
            @Valid @RequestBody CreateBindingRequest request
    ) {
        service.deleteBinding(request);
        return ResponseEntity.noContent().build();
    }
}
