package cl.salfa.bff.controller;

import cl.salfa.bff.dto.UserRequest;
import cl.salfa.bff.dto.UserResponse;
import cl.salfa.bff.service.UserBffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserBffController {

    private final UserBffService userService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> findAll() {
        return ResponseEntity.ok(
                userService.findAll()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                userService.findById(id)
        );
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(
            @RequestBody UserRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(
            @PathVariable Long id,
            @RequestBody UserRequest request
    ) {
        return ResponseEntity.ok(
                userService.update(id, request)
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<UserResponse> changeStatus(
            @PathVariable Long id,
            @RequestParam boolean active
    ) {
        return ResponseEntity.ok(
                userService.changeStatus(id, active)
        );
    }
}