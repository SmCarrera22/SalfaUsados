package cl.salfa.kafkaadmin.controller;

import cl.salfa.kafkaadmin.service.KafkaAdminService;

import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/kafka")
public class KafkaAdminController {

    private final KafkaAdminService service;

    public KafkaAdminController(
            KafkaAdminService service
    ) {
        this.service = service;
    }

    public record TopicRequest(
            String name,
            long retentionMs
    ) {}

    public record RetentionRequest(
            long retentionMs
    ) {}

    @GetMapping("/topics")
    public ResponseEntity<?> listTopics()
            throws Exception {

        return ResponseEntity.ok(
                service.listTopics()
        );
    }

    @GetMapping("/topics/{name}")
    public ResponseEntity<?> describeTopic(
            @PathVariable String name
    ) throws Exception {

        return ResponseEntity.ok(
                service.describeTopic(name)
        );
    }

    @PostMapping("/topics")
    public ResponseEntity<?> createTopic(
            @RequestBody TopicRequest request
    ) throws Exception {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        service.createTopic(
                                request.name(),
                                request.retentionMs()
                        )
                );
    }

    @PatchMapping("/topics/{name}/retention")
    public ResponseEntity<?> updateRetention(
            @PathVariable String name,
            @RequestBody RetentionRequest request
    ) throws Exception {

        return ResponseEntity.ok(
                service.updateRetention(
                        name,
                        request.retentionMs()
                )
        );
    }

    @DeleteMapping("/topics/{name}")
    public ResponseEntity<?> deleteTopic(
            @PathVariable String name
    ) throws Exception {

        return ResponseEntity.accepted()
                .body(service.deleteTopic(name));
    }

    @GetMapping("/groups")
    public ResponseEntity<?> listGroups()
            throws Exception {

        return ResponseEntity.ok(
                service.listGroups()
        );
    }

    @GetMapping("/groups/{groupId}")
    public ResponseEntity<?> describeGroup(
            @PathVariable String groupId
    ) throws Exception {

        return ResponseEntity.ok(
                service.describeGroup(groupId)
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleValidation(
            IllegalArgumentException exception
    ) {

        return ResponseEntity.badRequest().body(
                Map.of("error", exception.getMessage())
        );
    }

    @ExceptionHandler(ExecutionException.class)
    public ResponseEntity<?> handleKafkaError(
            ExecutionException exception
    ) {

        Throwable cause = exception.getCause();

        if (cause instanceof TopicExistsException) {

            return ResponseEntity.status(
                    HttpStatus.CONFLICT
            ).body(
                    Map.of("error", "El topico ya existe")
            );
        }

        if (cause instanceof UnknownTopicOrPartitionException) {

            return ResponseEntity.status(
                    HttpStatus.NOT_FOUND
            ).body(
                    Map.of("error", "Topico no encontrado")
            );
        }

        return ResponseEntity.status(
                HttpStatus.SERVICE_UNAVAILABLE
        ).body(
                Map.of(
                        "error",
                        "No fue posible completar la operacion Kafka"
                )
        );
    }

    @ExceptionHandler(java.util.concurrent.TimeoutException.class)
    public ResponseEntity<?> handleTimeout() {

        return ResponseEntity.status(
                HttpStatus.GATEWAY_TIMEOUT
        ).body(
                Map.of("error", "Tiempo de espera agotado")
        );
    }
}