package cl.salfa.workshop.messaging.listener;

import cl.salfa.workshop.config.RabbitNames;
import cl.salfa.workshop.messaging.dto.WorkshopOrderCreatedEvent;
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
public class WorkshopNotificationListener {

    private static final Logger log =
            LoggerFactory.getLogger(WorkshopNotificationListener.class);

    private final ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitNames.NOTIFICATION_QUEUE)
    public void consume(Message message, Channel channel)
            throws IOException {

        long tag = message.getMessageProperties().getDeliveryTag();

        try {
            String json = new String(
                    message.getBody(), StandardCharsets.UTF_8
            );

            WorkshopOrderCreatedEvent event = objectMapper.readValue(
                    json, WorkshopOrderCreatedEvent.class
            );

            if (event.eventId() == null
                    || event.orderId() == null
                    || event.vehicleId() == null) {
                throw new IllegalArgumentException(
                        "Evento de notificación incompleto"
                );
            }

            log.info(
                    "NOTIFICACION: orden={} vehiculo={} evento={}",
                    event.orderId(),
                    event.vehicleId(),
                    event.eventId()
            );

            channel.basicAck(tag, false);

        } catch (Exception ex) {
            log.error("Error procesando notificación", ex);
            channel.basicNack(tag, false, false);
        }
    }
}