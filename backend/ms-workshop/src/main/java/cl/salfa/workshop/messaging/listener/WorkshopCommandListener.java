package cl.salfa.workshop.messaging.listener;

import cl.salfa.workshop.config.RabbitNames;
import cl.salfa.workshop.dto.WorkshopOrderRequest;
import cl.salfa.workshop.dto.WorkshopOrderResponse;
import cl.salfa.workshop.messaging.dto.CreateWorkshopOrderCommand;
import cl.salfa.workshop.service.WorkshopOrderService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class WorkshopCommandListener {

    private static final Logger log =
            LoggerFactory.getLogger(WorkshopCommandListener.class);

    private final ObjectMapper objectMapper;
    private final WorkshopOrderService orderService;

    @RabbitListener(queues = RabbitNames.WORKSHOP_QUEUE)
    public void consume(Message message, Channel channel)
            throws IOException {

        long deliveryTag =
                message.getMessageProperties().getDeliveryTag();

        try {
            String json = new String(
                    message.getBody(),
                    java.nio.charset.StandardCharsets.UTF_8
            );

            CreateWorkshopOrderCommand command =
                    objectMapper.readValue(
                            json,
                            CreateWorkshopOrderCommand.class
                    );

            if (command.eventId() == null
                    || command.vehicleId() == null
                    || command.workshopId() == null
                    || command.entryDate() == null
                    || command.description() == null
                    || command.description().isBlank()) {
                throw new IllegalArgumentException(
                        "Comando incompleto"
                );
            }

            WorkshopOrderResponse result = orderService.create(
                    new WorkshopOrderRequest(
                            command.vehicleId(),
                            command.workshopId(),
                            command.entryDate(),
                            command.estimatedExitDate(),
                            command.description()
                    )
            );

            log.info(
                    "Orden procesada. eventId={}, orderId={}",
                    command.eventId(),
                    result.id()
            );

            channel.basicAck(deliveryTag, false);

        } catch (Exception ex) {

            log.error(
                    "Error procesando comando de taller: {}",
                    ex.getMessage(),
                    ex
            );

            channel.basicNack(
                    deliveryTag,
                    false,
                    false
            );
        }
    }
}