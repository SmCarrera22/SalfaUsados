package cl.salfa.workshop.messaging.kafka;

import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@ConditionalOnProperty(
        name = "salfa.kafka.enabled",
        havingValue = "true"
)
public class WorkshopKafkaErrorHandlerConfig {

    @Bean
    public DefaultErrorHandler workshopKafkaErrorHandler(
            KafkaTemplate<String, String> template,
            @Value("${salfa.kafka.topics.workshops-dlq}")
            String dlqTopic
    ) {
        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        template,
                        (record, exception) ->
                                new TopicPartition(
                                        dlqTopic,
                                        record.partition()
                                )
                );

        DefaultErrorHandler handler =
                new DefaultErrorHandler(
                        recoverer,
                        new FixedBackOff(1000L, 2L)
                );

        handler.setCommitRecovered(true);

        return handler;
    }
}
