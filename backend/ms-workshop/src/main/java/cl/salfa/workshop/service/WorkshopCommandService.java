
package cl.salfa.workshop.service;

import cl.salfa.workshop.dto.WorkshopOrderRequest;
import cl.salfa.workshop.dto.WorkshopOrderResponse;
import cl.salfa.workshop.messaging.dto.CreateWorkshopOrderCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkshopCommandService {

    private final JdbcTemplate jdbcTemplate;
    private final WorkshopOrderService orderService;

    @Transactional
    public ProcessedCommandResult process(
            CreateWorkshopOrderCommand command
    ) {
        if (command == null || command.eventId() == null) {
            throw new IllegalArgumentException(
                    "El comando debe incluir eventId"
            );
        }

        int inserted = jdbcTemplate.update(
                """
                INSERT INTO workshop_processed_commands (event_id)
                VALUES (?)
                ON CONFLICT (event_id) DO NOTHING
                """,
                command.eventId()
        );

        if (inserted == 0) {
            Long existingOrderId = jdbcTemplate.queryForObject(
                    """
                    SELECT order_id
                    FROM workshop_processed_commands
                    WHERE event_id = ?
                    """,
                    Long.class,
                    command.eventId()
            );

            if (existingOrderId == null) {
                throw new IllegalStateException(
                        "Comando duplicado sin orden confirmada"
                );
            }

            return new ProcessedCommandResult(
                    orderService.findById(existingOrderId),
                    false
            );
        }

        WorkshopOrderResponse order = orderService.create(
                new WorkshopOrderRequest(
                        command.vehicleId(),
                        command.workshopId(),
                        command.entryDate(),
                        command.estimatedExitDate(),
                        command.description()
                )
        );

        int updated = jdbcTemplate.update(
                """
                UPDATE workshop_processed_commands
                SET order_id = ?, processed_at = CURRENT_TIMESTAMP
                WHERE event_id = ?
                """,
                order.id(),
                command.eventId()
        );

        if (updated != 1) {
            throw new IllegalStateException(
                    "No se pudo registrar el comando procesado"
            );
        }

        return new ProcessedCommandResult(order, true);
    }

    public record ProcessedCommandResult(
            WorkshopOrderResponse order,
            boolean newlyCreated
    ) {}
}
