package cl.salfa.workshop.messaging.listener;

import cl.salfa.workshop.config.RabbitNames;
import cl.salfa.workshop.messaging.dto.WorkshopOrderCreatedEvent;
import cl.salfa.workshop.service.WorkshopAuditService;
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
public class WorkshopAuditListener {

    private static final Logger log =
            LoggerFactory.getLogger(WorkshopAuditListener.class);

    private final ObjectMapper objectMapper;
    private final WorkshopAuditService auditService;

    @RabbitListener(queues = RabbitNames.AUDIT_QUEUE)
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

            auditService.register(event);

            channel.basicAck(tag, false);

            log.info(
                    "AUDITORIA PERSISTIDA: eventId={} orderId={}",
                    event.eventId(), event.orderId()
            );

        } catch (IllegalArgumentException ex) {
            log.error("Evento de auditoría inválido", ex);
            channel.basicNack(tag, false, false);

        } catch (Exception ex) {
            log.error("Error procesando auditoría", ex);
            channel.basicNack(tag, false, true);
        }
    }
}