package cl.salfa.workshop.messaging.listener;

import cl.salfa.workshop.config.RabbitNames;
import cl.salfa.workshop.service.WorkshopDeadLetterService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class WorkshopDeadLetterListener {

    private static final Logger log =
            LoggerFactory.getLogger(WorkshopDeadLetterListener.class);

    private final WorkshopDeadLetterService deadLetterService;

    @RabbitListener(queues = RabbitNames.WORKSHOP_DLQ)
    public void consumeWorkshop(Message message, Channel channel)
            throws IOException {
        process(RabbitNames.WORKSHOP_DLQ, message, channel);
    }

    @RabbitListener(queues = RabbitNames.NOTIFICATION_DLQ)
    public void consumeNotification(Message message, Channel channel)
            throws IOException {
        process(RabbitNames.NOTIFICATION_DLQ, message, channel);
    }

    @RabbitListener(queues = RabbitNames.AUDIT_DLQ)
    public void consumeAudit(Message message, Channel channel)
            throws IOException {
        process(RabbitNames.AUDIT_DLQ, message, channel);
    }

    private void process(
            String sourceQueue,
            Message message,
            Channel channel
    ) throws IOException {

        long tag = message.getMessageProperties().getDeliveryTag();

        String payload = new String(
                message.getBody(), StandardCharsets.UTF_8
        );

        String messageId =
                message.getMessageProperties().getMessageId();

        try {
            deadLetterService.register(
                    sourceQueue, messageId, payload
            );

            channel.basicAck(tag, false);

            log.warn(
                    "DLQ REGISTRADA: queue={} messageId={}",
                    sourceQueue, messageId
            );

        } catch (Exception ex) {
            log.error(
                    "No fue posible persistir mensaje DLQ: {}",
                    sourceQueue, ex
            );

            channel.basicNack(tag, false, true);
        }
    }
}