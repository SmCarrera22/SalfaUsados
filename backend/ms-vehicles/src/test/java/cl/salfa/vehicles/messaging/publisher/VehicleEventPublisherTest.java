package cl.salfa.vehicles.messaging.publisher;

import cl.salfa.vehicles.messaging.dto.VehicleEvent;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VehicleEventPublisherTest {

    private KafkaTemplate<String, String> kafkaTemplate;
    private ObjectMapper objectMapper;
    private VehicleEventPublisher publisher;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);
        objectMapper = mock(ObjectMapper.class);

        publisher = new VehicleEventPublisher(
                kafkaTemplate,
                objectMapper
        );
    }

    private VehicleEvent validEvent() {
        return new VehicleEvent(
                UUID.randomUUID(),
                "VEHICLE_CREATED",
                1,
                1L,
                "TESTVIN0000000001",
                "SANTIAGO",
                Instant.now()
        );
    }

    @Test
    void shouldPublishValidEventToCorrectTopic() {

        VehicleEvent event = validEvent();

        String json = """
                {"eventType":"VEHICLE_CREATED"}
                """.trim();

        when(objectMapper.writeValueAsString(event))
                .thenReturn(json);

        RecordMetadata metadata = mock(RecordMetadata.class);

        when(metadata.partition()).thenReturn(2);
        when(metadata.offset()).thenReturn(10L);

        @SuppressWarnings("unchecked")
        SendResult<String, String> sendResult =
                mock(SendResult.class);

        when(sendResult.getRecordMetadata())
                .thenReturn(metadata);

        when(kafkaTemplate.send(
                "vehicle.events",
                event.vin(),
                json
        )).thenReturn(
                CompletableFuture.completedFuture(sendResult)
        );

        publisher.publish(event);

        verify(kafkaTemplate, times(1)).send(
                "vehicle.events",
                "TESTVIN0000000001",
                json
        );
    }

    @Test
    void shouldRejectNullEvent() {

        assertThrows(
                NullPointerException.class,
                () -> publisher.publish(null)
        );

        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void shouldRejectEventWithoutVin() {

        VehicleEvent invalidEvent = new VehicleEvent(
                UUID.randomUUID(),
                "VEHICLE_CREATED",
                1,
                1L,
                null,
                "SANTIAGO",
                Instant.now()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> publisher.publish(invalidEvent)
        );

        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void shouldRejectEventWithoutId() {

        VehicleEvent invalidEvent = new VehicleEvent(
                null,
                "VEHICLE_CREATED",
                1,
                1L,
                "TESTVIN0000000001",
                "SANTIAGO",
                Instant.now()
        );

        assertThrows(
                NullPointerException.class,
                () -> publisher.publish(invalidEvent)
        );

        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void shouldHandleAsynchronousSendFailure() {

        VehicleEvent event = validEvent();

        String json = "{\"eventType\":\"VEHICLE_CREATED\"}";

        when(objectMapper.writeValueAsString(event))
                .thenReturn(json);

        CompletableFuture<SendResult<String, String>> future =
                new CompletableFuture<>();

        when(kafkaTemplate.send(
                "vehicle.events",
                event.vin(),
                json
        )).thenReturn(future);

        publisher.publish(event);

        future.completeExceptionally(
                new RuntimeException("Kafka no disponible")
        );

        verify(kafkaTemplate).send(
                "vehicle.events",
                event.vin(),
                json
        );

        assertTrue(future.isCompletedExceptionally());
    }
}
