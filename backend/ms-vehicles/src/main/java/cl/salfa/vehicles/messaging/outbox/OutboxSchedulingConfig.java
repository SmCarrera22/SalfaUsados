package cl.salfa.vehicles.messaging.outbox;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnProperty(
        name = "salfa.kafka.enabled",
        havingValue = "true"
)
@ConditionalOnProperty(
        name = "salfa.outbox.dispatcher.enabled",
        havingValue = "true"
)
public class OutboxSchedulingConfig {
}
