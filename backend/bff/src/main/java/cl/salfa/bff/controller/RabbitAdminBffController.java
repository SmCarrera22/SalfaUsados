package cl.salfa.bff.controller;

import cl.salfa.bff.service.RabbitAdminBffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rabbit-admin")
@RequiredArgsConstructor
public class RabbitAdminBffController {

    private final RabbitAdminBffService service;

    @PostMapping("/queues")
    public ResponseEntity<String> createQueue(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth,
            @RequestBody String body
    ) {
        return service.create(
                "/api/rabbit-admin/queues",
                bearerToken(auth),
                body
        );
    }

    @PostMapping("/exchanges")
    public ResponseEntity<String> createExchange(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth,
            @RequestBody String body
    ) {
        return service.create(
                "/api/rabbit-admin/exchanges",
                bearerToken(auth),
                body
        );
    }

    @PostMapping("/bindings")
    public ResponseEntity<String> createBinding(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth,
            @RequestBody String body
    ) {
        return service.create(
                "/api/rabbit-admin/bindings",
                bearerToken(auth),
                body
        );
    }

    @DeleteMapping("/queues/{name}")
    public ResponseEntity<Void> deleteQueue(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth,
            @PathVariable String name
    ) {
        return service.deleteQueue(
                validName(name),
                bearerToken(auth)
        );
    }

    @DeleteMapping("/exchanges/{name}")
    public ResponseEntity<Void> deleteExchange(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth,
            @PathVariable String name
    ) {
        return service.deleteExchange(
                validName(name),
                bearerToken(auth)
        );
    }

    @DeleteMapping("/bindings")
    public ResponseEntity<Void> deleteBinding(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String auth,
            @RequestBody String body
    ) {
        return service.deleteBinding(
                bearerToken(auth),
                body
        );
    }

    private String bearerToken(String authorization) {

        if (authorization == null
                || !authorization.startsWith("Bearer ")
                || authorization.substring(7).isBlank()) {
            throw new IllegalArgumentException(
                    "Se requiere un Bearer token"
            );
        }

        return authorization.substring(7);
    }

    private String validName(String name) {

        if (name == null ||
                !name.matches("^dyn\\.[a-zA-Z0-9._-]{1,100}$")) {
            throw new IllegalArgumentException(
                    "Nombre de recurso dinámico inválido"
            );
        }

        return name;
    }
}
