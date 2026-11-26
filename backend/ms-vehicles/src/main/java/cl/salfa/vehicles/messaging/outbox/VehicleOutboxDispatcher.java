package cl.salfa.vehicles.messaging.outbox;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "salfa.kafka.enabled",
        havingValue = "true"
)
@ConditionalOnProperty(
        name = "salfa.outbox.dispatcher.enabled",
        havingValue = "true"
)
public class VehicleOutboxDispatcher {

    private static final Logger log =
            LoggerFactory.getLogger(
                    VehicleOutboxDispatcher.class
            );

    private static final String TOPIC = "vehicle.events";

    private static final int MAX_BATCH = 10;
    private static final long SEND_TIMEOUT_SECONDS = 20L;

    private final VehicleOutboxClaimService claimService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(
            fixedDelayString = "${salfa.outbox.dispatcher.poll-ms:5000}"
    )
    public void dispatch() {

        for (int i = 0; i < MAX_BATCH; i++) {

            if (!processOnce()) {
                break;
            }
        }
    }

    public boolean processOnce() {

        Optional<OutboxClaim> result =
                claimService.claimNext();

        if (result.isEmpty()) {
            return false;
        }

        OutboxClaim claim = result.get();

        try {

            var sendResult = kafkaTemplate.send(
                    TOPIC,
                    claim.eventKey(),
                    claim.payload()
            ).get(
                    SEND_TIMEOUT_SECONDS,
                    TimeUnit.SECONDS
            );

            boolean confirmed =
                    claimService.markPublished(claim);

            if (!confirmed) {
                log.warn(
                        "Kafka confirmó el evento {}, "
                                + "pero el token Outbox ya no es vigente",
                        claim.eventId()
                );
                return true;
            }

            log.info(
                    "Outbox publicado: eventId={}, "
                            + "partition={}, offset={}",
                    claim.eventId(),
                    sendResult.getRecordMetadata().partition(),
                    sendResult.getRecordMetadata().offset()
            );

        } catch (InterruptedException ex) {

            try {
                recordFailure(claim, ex);
            } finally {
                Thread.currentThread().interrupt();
            }

            return false;

        } catch (Exception ex) {

            recordFailure(claim, ex);
        }

        return true;
    }

    private void recordFailure(
            OutboxClaim claim,
            Exception ex
    ) {

        log.error(
                "Fallo publicando evento Outbox {}",
                claim.eventId(),
                ex
        );

        boolean updated = claimService.markRetry(
                claim,
                ex.getMessage()
        );

        if (!updated) {
            log.warn(
                    "No se pudo registrar el reintento del evento {}",
                    claim.eventId()
            );
        }
    }
}
