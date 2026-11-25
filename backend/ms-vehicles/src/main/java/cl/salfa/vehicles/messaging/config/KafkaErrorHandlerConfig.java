package cl.salfa.vehicles.messaging.config;

import org.apache.kafka.common.TopicPartition;
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
public class KafkaErrorHandlerConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            KafkaTemplate<String, String> kafkaTemplate
    ) {

        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        kafkaTemplate,
                        (record, exception) ->
                                new TopicPartition(
                                        record.topic() + ".dlq",
                                        record.partition()
                                )
                );

        // Un error se intenta procesar tres veces en total:
        // el intento inicial y dos reintentos.
        FixedBackOff backOff = new FixedBackOff(
                1000L,
                2L
        );

        recoverer.setFailIfSendResultIsError(true);

        DefaultErrorHandler handler =
                new DefaultErrorHandler(
                        recoverer,
                        backOff
                );

        // Requiere MANUAL_IMMEDIATE.
        handler.setCommitRecovered(true);

        return handler;
    }
}
