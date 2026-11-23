package cl.salfa.vehicles.messaging.listener;

import cl.salfa.vehicles.messaging.dto.VehicleEvent;
import cl.salfa.vehicles.service.VehicleHistoryService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.Acknowledgment;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class VehicleEventListenerTest {

    private ObjectMapper objectMapper;
    private VehicleHistoryService historyService;
    private Acknowledgment acknowledgment;
    private VehicleEventListener listener;

    private static final String VIN = "TESTVIN0000000001";

    @BeforeEach
    void setUp() {
        objectMapper = mock(ObjectMapper.class);
        historyService = mock(VehicleHistoryService.class);
        acknowledgment = mock(Acknowledgment.class);

        listener = new VehicleEventListener(
                objectMapper,
                historyService
        );
    }

    @Test
    void shouldPersistAndAcknowledgeValidEvent() throws Exception {

        VehicleEvent event = validEvent();

        ConsumerRecord<String, String> record =
                record(event.vin(), 5L);

        when(objectMapper.readValue(
                anyString(),
                eq(VehicleEvent.class)
        )).thenReturn(event);

        when(historyService.register(event)).thenReturn(true);

        listener.consume(record, acknowledgment);

        var order = inOrder(historyService, acknowledgment);

        order.verify(historyService).register(event);
        order.verify(acknowledgment).acknowledge();
    }

    @Test
    void shouldAcknowledgeDuplicateEventWithoutFailing()
            throws Exception {

        VehicleEvent event = validEvent();

        when(objectMapper.readValue(
                anyString(),
                eq(VehicleEvent.class)
        )).thenReturn(event);

        when(historyService.register(event)).thenReturn(false);

        listener.consume(
                record(event.vin(), 6L),
                acknowledgment
        );

        verify(historyService).register(event);
        verify(acknowledgment).acknowledge();
    }

    @Test
    void shouldNotAcknowledgeWhenPersistenceFails()
            throws Exception {

        VehicleEvent event = validEvent();

        when(objectMapper.readValue(
                anyString(),
                eq(VehicleEvent.class)
        )).thenReturn(event);

        when(historyService.register(event))
                .thenThrow(new IllegalStateException(
                        "PostgreSQL no disponible"
                ));

        assertThrows(
                IllegalStateException.class,
                () -> listener.consume(
                        record(event.vin(), 7L),
                        acknowledgment
                )
        );

        verify(historyService).register(event);
        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    void shouldRejectEventWithDifferentVin() throws Exception {

        VehicleEvent event = new VehicleEvent(
                UUID.randomUUID(),
                "VEHICLE_CREATED",
                1,
                1L,
                "VIN-DIFFERENT",
                "SANTIAGO",
                Instant.now()
        );

        when(objectMapper.readValue(
                anyString(),
                eq(VehicleEvent.class)
        )).thenReturn(event);

        assertThrows(
                IllegalStateException.class,
                () -> listener.consume(
                        record(VIN, 8L),
                        acknowledgment
                )
        );

        verifyNoInteractions(historyService);
        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    void shouldRejectUnsupportedEventVersion()
            throws Exception {

        VehicleEvent event = new VehicleEvent(
                UUID.randomUUID(),
                "VEHICLE_CREATED",
                99,
                1L,
                VIN,
                "SANTIAGO",
                Instant.now()
        );

        when(objectMapper.readValue(
                anyString(),
                eq(VehicleEvent.class)
        )).thenReturn(event);

        assertThrows(
                IllegalStateException.class,
                () -> listener.consume(
                        record(VIN, 9L),
                        acknowledgment
                )
        );

        verifyNoInteractions(historyService);
        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    void shouldRejectEventWithoutId() throws Exception {

        VehicleEvent event = new VehicleEvent(
                null,
                "VEHICLE_CREATED",
                1,
                1L,
                VIN,
                "SANTIAGO",
                Instant.now()
        );

        when(objectMapper.readValue(
                anyString(),
                eq(VehicleEvent.class)
        )).thenReturn(event);

        assertThrows(
                IllegalStateException.class,
                () -> listener.consume(
                        record(VIN, 10L),
                        acknowledgment
                )
        );

        verifyNoInteractions(historyService);
        verify(acknowledgment, never()).acknowledge();
    }

    @Test
    void shouldRejectMalformedJson() throws Exception {

        when(objectMapper.readValue(
                anyString(),
                eq(VehicleEvent.class)
        )).thenThrow(
                new IllegalArgumentException("JSON inválido")
        );

        assertThrows(
                IllegalStateException.class,
                () -> listener.consume(
                        record(VIN, 11L),
                        acknowledgment
                )
        );

        verifyNoInteractions(historyService);
        verify(acknowledgment, never()).acknowledge();
    }

    private VehicleEvent validEvent() {

        return new VehicleEvent(
                UUID.randomUUID(),
                "VEHICLE_CREATED",
                1,
                1L,
                VIN,
                "SANTIAGO",
                Instant.now()
        );
    }

    private ConsumerRecord<String, String> record(
            String vin,
            long offset
    ) {

        return new ConsumerRecord<>(
                "vehicle.events",
                2,
                offset,
                vin,
                "{\"eventType\":\"VEHICLE_CREATED\"}"
        );
    }
}
