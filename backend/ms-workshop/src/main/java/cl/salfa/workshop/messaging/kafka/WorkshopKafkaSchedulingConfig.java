package cl.salfa.workshop.messaging.kafka;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnProperty(
        name = "salfa.workshop.outbox.enabled",
        havingValue = "true"
)
public class WorkshopKafkaSchedulingConfig {
}
