package cl.salfa.vehicles.messaging.outbox;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleOutboxDispatcherTest {

    @Mock
    private VehicleOutboxClaimService claimService;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private VehicleOutboxDispatcher dispatcher;

    private OutboxClaim claim;

    @BeforeEach
    void setUp() {

        dispatcher = new VehicleOutboxDispatcher(
                claimService,
                kafkaTemplate
        );

        claim = new OutboxClaim(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "TESTVIN0000000001",
                "{\"eventType\":\"VEHICLE_CREATED\"}",
                1
        );
    }

    @Test
    void shouldPublishAndConfirmOutboxEvent() {

        when(claimService.claimNext())
                .thenReturn(Optional.of(claim));

        SendResult<String, String> sendResult =
                mock(SendResult.class);

        org.apache.kafka.clients.producer.RecordMetadata metadata =
                mock(
                        org.apache.kafka.clients.producer.RecordMetadata.class
                );

        when(sendResult.getRecordMetadata())
                .thenReturn(metadata);

        when(kafkaTemplate.send(
                eq("vehicle.events"),
                eq(claim.eventKey()),
                eq(claim.payload())
        )).thenReturn(
                CompletableFuture.completedFuture(sendResult)
        );

        when(claimService.markPublished(claim))
                .thenReturn(true);

        assertTrue(dispatcher.processOnce());

        verify(claimService).markPublished(claim);

        verify(claimService, never())
                .markRetry(any(), any());
    }

    @Test
    void shouldRetryWhenKafkaFails() {

        when(claimService.claimNext())
                .thenReturn(Optional.of(claim));

        CompletableFuture<SendResult<String, String>> failed =
                new CompletableFuture<>();

        failed.completeExceptionally(
                new IllegalStateException("Kafka unavailable")
        );

        when(kafkaTemplate.send(
                anyString(),
                anyString(),
                anyString()
        )).thenReturn(failed);

        when(claimService.markRetry(
                eq(claim),
                anyString()
        )).thenReturn(true);

        assertTrue(dispatcher.processOnce());

        verify(claimService, never())
                .markPublished(any());

        verify(claimService).markRetry(
                eq(claim),
                anyString()
        );
    }

    @Test
    void shouldStopWhenNoPendingEventsExist() {

        when(claimService.claimNext())
                .thenReturn(Optional.empty());

        assertFalse(dispatcher.processOnce());

        verifyNoInteractions(kafkaTemplate);
    }


    @Test
    void shouldStopBatchWhenNoMoreEventsExist() {

        when(claimService.claimNext())
                .thenReturn(
                        Optional.of(claim),
                        Optional.empty()
                );

        SendResult<String, String> sendResult =
                mock(SendResult.class);

        var metadata = mock(
                org.apache.kafka.clients.producer.RecordMetadata.class
        );

        when(sendResult.getRecordMetadata())
                .thenReturn(metadata);

        when(kafkaTemplate.send(
                eq("vehicle.events"),
                eq(claim.eventKey()),
                eq(claim.payload())
        )).thenReturn(
                CompletableFuture.completedFuture(sendResult)
        );

        when(claimService.markPublished(claim))
                .thenReturn(true);

        dispatcher.dispatch();

        verify(claimService, times(2)).claimNext();

        verify(kafkaTemplate, times(1))
                .send(
                        eq("vehicle.events"),
                        eq(claim.eventKey()),
                        eq(claim.payload())
                );

        verify(claimService, times(1))
                .markPublished(claim);
    }


}
