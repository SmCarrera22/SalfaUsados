package cl.salfa.rabbitadmin.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class RabbitAdminExceptionHandler {

    @ExceptionHandler({
            IllegalArgumentException.class,
            MethodArgumentNotValidException.class
    })
    public ResponseEntity<Map<String, String>> badRequest(
            Exception ex
    ) {
        return ResponseEntity.badRequest().body(
                Map.of("error", "Solicitud inválida")
        );
    }

    @ExceptionHandler(
            org.springframework.amqp.AmqpException.class
    )
    public ResponseEntity<Map<String, String>> brokerFailure(
            Exception ex
    ) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error",
                        "No fue posible completar la operación RabbitMQ"
                ));
    }
}