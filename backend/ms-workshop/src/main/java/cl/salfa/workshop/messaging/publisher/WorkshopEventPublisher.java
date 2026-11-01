package cl.salfa.workshop.messaging.publisher;

import cl.salfa.workshop.config.RabbitNames;
import cl.salfa.workshop.messaging.dto.WorkshopOrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class WorkshopEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public void publishOrderCreated(WorkshopOrderCreatedEvent event) {

        if (event == null || event.eventId() == null
                || event.orderId() == null) {
            throw new IllegalArgumentException(
                    "El evento de creación de orden es inválido"
            );
        }

        String json = objectMapper.writeValueAsString(event);

        rabbitTemplate.convertAndSend(
                RabbitNames.EVENT_EXCHANGE,
                "workshop.order.created",
                json,
                message -> {
                    message.getMessageProperties()
                            .setContentType("application/json");

                    message.getMessageProperties()
                            .setMessageId(event.eventId().toString());

                    return message;
                }
        );
    }
}