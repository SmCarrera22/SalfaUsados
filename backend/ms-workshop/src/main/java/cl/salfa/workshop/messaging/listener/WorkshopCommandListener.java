
package cl.salfa.workshop.messaging.listener;

import cl.salfa.workshop.config.RabbitNames;
import cl.salfa.workshop.messaging.dto.CreateWorkshopOrderCommand;
import cl.salfa.workshop.service.WorkshopCommandService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class WorkshopCommandListener {

    private static final Logger log =
            LoggerFactory.getLogger(WorkshopCommandListener.class);

    private final ObjectMapper objectMapper;
    private final WorkshopCommandService commandService;

    @RabbitListener(queues = RabbitNames.WORKSHOP_QUEUE)
    public void consume(Message message, Channel channel)
            throws IOException {

        long tag = message.getMessageProperties().getDeliveryTag();

        CreateWorkshopOrderCommand command;

        try {
            String json = new String(
                    message.getBody(),
                    StandardCharsets.UTF_8
            );

            command = objectMapper.readValue(
                    json,
                    CreateWorkshopOrderCommand.class
            );

            if (command == null
                    || command.eventId() == null
                    || command.vehicleId() == null
                    || command.workshopId() == null
                    || command.entryDate() == null
                    || command.description() == null
                    || command.description().isBlank()) {
                throw new IllegalArgumentException(
                        "Comando de taller incompleto"
                );
            }

        } catch (Exception ex) {
            log.error("Comando de taller inválido", ex);
            channel.basicNack(tag, false, false);
            return;
        }

        try {
            var result = commandService.process(command);

            log.info(
                    "COMANDO PROCESADO: eventId={}, orderId={}, nuevo={}",
                    command.eventId(),
                    result.order().id(),
                    result.newlyCreated()
            );

            channel.basicAck(tag, false);

        } catch (Exception ex) {
            log.error(
                    "Fallo procesando comando eventId={}",
                    command.eventId(),
                    ex
            );

            // Política provisional: conservar el mensaje en DLQ.
            // Falta clasificar fallos transitorios y aplicar reintentos acotados.
            channel.basicNack(tag, false, false);
        }
    }
}
