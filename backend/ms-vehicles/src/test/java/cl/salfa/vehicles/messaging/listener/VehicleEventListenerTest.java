package cl.salfa.vehicles.messaging.listener;

import cl.salfa.vehicles.messaging.dto.VehicleEvent;
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
    private Acknowledgment acknowledgment;
    private VehicleEventListener listener;

    @BeforeEach
    void setUp() {
        objectMapper = mock(ObjectMapper.class);
        acknowledgment = mock(Acknowledgment.class);
        listener = new VehicleEventListener(objectMapper);
    }

    @Test
    void shouldAcknowledgeValidEvent() throws Exception {

        String vin = "TESTVIN0000000001";

        VehicleEvent event = new VehicleEvent(
                UUID.randomUUID(),
                "VEHICLE_CREATED",
                1,
                1L,
                vin,
                "SANTIAGO",
                Instant.now()
        );

        ConsumerRecord<String, String> record =
                new ConsumerRecord<>(
                        "vehicle.events",
                        2,
                        5L,
                        vin,
                        "{\"eventType\":\"VEHICLE_CREATED\"}"
                );

        when(objectMapper.readValue(
                anyString(),
                eq(VehicleEvent.class)
        )).thenReturn(event);

        listener.consume(record, acknowledgment);

        verify(acknowledgment, times(1)).acknowledge();
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

        ConsumerRecord<String, String> record =
                new ConsumerRecord<>(
                        "vehicle.events",
                        2,
                        6L,
                        "TESTVIN0000000001",
                        "{}"
                );

        when(objectMapper.readValue(
                anyString(),
                eq(VehicleEvent.class)
        )).thenReturn(event);

        assertThrows(
                IllegalStateException.class,
                () -> listener.consume(record, acknowledgment)
        );

        verify(acknowledgment, never()).acknowledge();
    }


    @Test
    void shouldRejectUnsupportedEventVersion() throws Exception {

        VehicleEvent event = new VehicleEvent(
                UUID.randomUUID(),
                "VEHICLE_CREATED",
                99,
                1L,
                "TESTVIN0000000001",
                "SANTIAGO",
                Instant.now()
        );

        ConsumerRecord<String, String> record =
                new ConsumerRecord<>(
                        "vehicle.events",
                        2,
                        10L,
                        event.vin(),
                        "{}"
                );

        when(objectMapper.readValue(
                anyString(),
                eq(VehicleEvent.class)
        )).thenReturn(event);

        assertThrows(
                IllegalStateException.class,
                () -> listener.consume(record, acknowledgment)
        );

        verify(acknowledgment, never()).acknowledge();
    }


    @Test
    void shouldRejectEventWithoutId() throws Exception {

        VehicleEvent event = new VehicleEvent(
                null,
                "VEHICLE_CREATED",
                1,
                1L,
                "TESTVIN0000000001",
                "SANTIAGO",
                Instant.now()
        );

        ConsumerRecord<String, String> record =
                new ConsumerRecord<>(
                        "vehicle.events",
                        2,
                        11L,
                        event.vin(),
                        "{}"
                );

        when(objectMapper.readValue(
                anyString(),
                eq(VehicleEvent.class)
        )).thenReturn(event);

        assertThrows(
                IllegalStateException.class,
                () -> listener.consume(record, acknowledgment)
        );

        verify(acknowledgment, never()).acknowledge();
    }


    @Test
    void shouldRejectMalformedJson() throws Exception {

        ConsumerRecord<String, String> record =
                new ConsumerRecord<>(
                        "vehicle.events",
                        2,
                        12L,
                        "TESTVIN0000000001",
                        "{json-invalid"
                );

        when(objectMapper.readValue(
                anyString(),
                eq(VehicleEvent.class)
        )).thenThrow(
                new IllegalArgumentException("JSON inválido")
        );

        assertThrows(
                IllegalStateException.class,
                () -> listener.consume(record, acknowledgment)
        );

        verify(acknowledgment, never()).acknowledge();
    }


}
