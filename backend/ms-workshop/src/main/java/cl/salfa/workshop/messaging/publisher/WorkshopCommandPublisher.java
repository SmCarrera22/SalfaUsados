package cl.salfa.workshop.messaging.publisher;

import cl.salfa.workshop.config.RabbitNames;
import cl.salfa.workshop.messaging.dto.CreateWorkshopOrderCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class WorkshopCommandPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public void publish(CreateWorkshopOrderCommand command) {

        if (command == null || command.eventId() == null) {
            throw new IllegalArgumentException(
                    "El comando debe contener un identificador"
            );
        }

        String json = objectMapper.writeValueAsString(command);

        rabbitTemplate.convertAndSend(
                RabbitNames.COMMAND_EXCHANGE,
                RabbitNames.WORKSHOP_KEY,
                json,
                message -> {
                    message.getMessageProperties().setContentType(
                            "application/json"
                    );
                    message.getMessageProperties().setMessageId(
                            command.eventId().toString()
                    );
                    return message;
                }
        );
    }
}