package cl.salfa.user.controller;

import cl.salfa.user.dto.UserRequest;
import cl.salfa.user.dto.UserResponse;
import cl.salfa.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> findAll() {
        return ResponseEntity.ok(userService.findAll());
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
            @Valid @RequestBody UserRequest request
    ) {
        UserResponse response = userService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request
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