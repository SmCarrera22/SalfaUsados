package cl.salfa.workshop.messaging.kafka;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "salfa.workshop.outbox.enabled",
        havingValue = "true"
)
public class WorkshopKafkaOutboxDispatcher {

    private static final Logger log =
            LoggerFactory.getLogger(
                    WorkshopKafkaOutboxDispatcher.class
            );

    private final JdbcTemplate jdbcTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    @Value("${salfa.kafka.topics.workshops}")
    private String workshopTopic;

    private record ClaimedEvent(
            UUID eventId,
            String eventKey,
            String payload,
            UUID claimToken
    ) {}

    @Scheduled(
            fixedDelayString =
                    "${salfa.workshop.outbox.poll-ms:5000}"
    )
    public void dispatch() {

        for (int i = 0; i < 10; i++) {
            if (!processOnce()) {
                break;
            }
        }
    }

    public boolean processOnce() {

        UUID token = UUID.randomUUID();

        List<ClaimedEvent> claimed = jdbcTemplate.query(
                """
                UPDATE workshop_kafka_outbox
                SET
                    status = 'PROCESSING',
                    claim_token = ?,
                    claimed_until =
                        CURRENT_TIMESTAMP + INTERVAL '60 seconds',
                    attempts = attempts + 1
                WHERE event_id = (
                    SELECT event_id
                    FROM workshop_kafka_outbox
                    WHERE (
                        status = 'PENDING'
                        AND next_attempt_at <= CURRENT_TIMESTAMP
                    ) OR (
                        status = 'PROCESSING'
                        AND claimed_until < CURRENT_TIMESTAMP
                    )
                    ORDER BY created_at
                    FOR UPDATE SKIP LOCKED
                    LIMIT 1
                )
                RETURNING event_id, event_key, payload, claim_token
                """,
                (rs, rowNum) -> new ClaimedEvent(
                        rs.getObject("event_id", UUID.class),
                        rs.getString("event_key"),
                        rs.getString("payload"),
                        rs.getObject("claim_token", UUID.class)
                ),
                token
        );

        if (claimed.isEmpty()) {
            return false;
        }

        ClaimedEvent event = claimed.getFirst();

        try {
            var result = kafkaTemplate.send(
                    workshopTopic,
                    event.eventKey(),
                    event.payload()
            ).get(20, TimeUnit.SECONDS);

            int updated = jdbcTemplate.update(
                    """
                    UPDATE workshop_kafka_outbox
                    SET
                        status = 'PUBLISHED',
                        published_at = CURRENT_TIMESTAMP,
                        claim_token = NULL,
                        claimed_until = NULL,
                        last_error = NULL
                    WHERE event_id = ?
                      AND claim_token = ?
                      AND status = 'PROCESSING'
                    """,
                    event.eventId(),
                    event.claimToken()
            );

            if (updated != 1) {
                log.warn(
                        "Kafka confirmó {}, pero la reclamación cambió",
                        event.eventId()
                );
            } else {
                log.info(
                        "Evento workshop publicado: eventId={}, partition={}, offset={}",
                        event.eventId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset()
                );
            }

        } catch (InterruptedException ex) {
            markRetry(event, ex);
            Thread.currentThread().interrupt();
            return false;

        } catch (Exception ex) {
            markRetry(event, ex);
        }

        return true;
    }

    private void markRetry(
            ClaimedEvent event,
            Exception ex
    ) {
        log.error(
                "Error publicando evento workshop {}",
                event.eventId(),
                ex
        );

        jdbcTemplate.update(
                """
                UPDATE workshop_kafka_outbox
                SET
                    status = CASE
                        WHEN attempts >= 5 THEN 'FAILED'
                        ELSE 'PENDING'
                    END,
                    next_attempt_at =
                        CURRENT_TIMESTAMP + INTERVAL '10 seconds',
                    last_error = ?,
                    claim_token = NULL,
                    claimed_until = NULL
                WHERE event_id = ?
                  AND claim_token = ?
                  AND status = 'PROCESSING'
                """,
                String.valueOf(ex.getMessage()),
                event.eventId(),
                event.claimToken()
        );
    }
}
