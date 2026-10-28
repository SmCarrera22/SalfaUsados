package cl.salfa.workshop.controller;

import cl.salfa.workshop.dto.WorkshopRequest;
import cl.salfa.workshop.entity.Workshop;
import cl.salfa.workshop.service.WorkshopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workshops")
@RequiredArgsConstructor
public class WorkshopController {

    private final WorkshopService workshopService;

    @GetMapping
    public ResponseEntity<List<Workshop>> findAll() {
        return ResponseEntity.ok(workshopService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Workshop> findById(@PathVariable Long id) {
        return ResponseEntity.ok(workshopService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Workshop> create(
            @Valid @RequestBody WorkshopRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workshopService.create(request));
    }
}