package cl.salfa.workshop.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitTopologyConfig {

    @Bean
    public DirectExchange commandExchange() {
        return new DirectExchange(RabbitNames.COMMAND_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange eventExchange() {
        return new TopicExchange(RabbitNames.EVENT_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(RabbitNames.DLX, true, false);
    }

    private Queue functionalQueue(String name, String deadLetterKey) {
        return QueueBuilder.durable(name)
                .deadLetterExchange(RabbitNames.DLX)
                .deadLetterRoutingKey(deadLetterKey)
                .maxLength(10000)
                .build();
    }

    @Bean
    public Queue workshopQueue() {
        return functionalQueue(RabbitNames.WORKSHOP_QUEUE, RabbitNames.WORKSHOP_DLQ);
    }

    @Bean
    public Queue notificationQueue() {
        return functionalQueue(RabbitNames.NOTIFICATION_QUEUE, RabbitNames.NOTIFICATION_DLQ);
    }

    @Bean
    public Queue auditQueue() {
        return functionalQueue(RabbitNames.AUDIT_QUEUE, RabbitNames.AUDIT_DLQ);
    }

    @Bean
    public Queue workshopDlq() {
        return QueueBuilder.durable(RabbitNames.WORKSHOP_DLQ).build();
    }

    @Bean
    public Queue notificationDlq() {
        return QueueBuilder.durable(RabbitNames.NOTIFICATION_DLQ).build();
    }

    @Bean
    public Queue auditDlq() {
        return QueueBuilder.durable(RabbitNames.AUDIT_DLQ).build();
    }

    @Bean
    public Declarables bindings(
            DirectExchange commandExchange,
            TopicExchange eventExchange,
            DirectExchange deadLetterExchange,
            Queue workshopQueue,
            Queue notificationQueue,
            Queue auditQueue,
            Queue workshopDlq,
            Queue notificationDlq,
            Queue auditDlq
    ) {
        return new Declarables(
                BindingBuilder.bind(workshopQueue)
                        .to(commandExchange)
                        .with(RabbitNames.WORKSHOP_KEY),

                BindingBuilder.bind(notificationQueue)
                        .to(eventExchange)
                        .with(RabbitNames.NOTIFICATION_KEY),

                BindingBuilder.bind(auditQueue)
                        .to(eventExchange)
                        .with(RabbitNames.AUDIT_KEY),

                BindingBuilder.bind(workshopDlq)
                        .to(deadLetterExchange)
                        .with(RabbitNames.WORKSHOP_DLQ),

                BindingBuilder.bind(notificationDlq)
                        .to(deadLetterExchange)
                        .with(RabbitNames.NOTIFICATION_DLQ),

                BindingBuilder.bind(auditDlq)
                        .to(deadLetterExchange)
                        .with(RabbitNames.AUDIT_DLQ)
        );
    }
}