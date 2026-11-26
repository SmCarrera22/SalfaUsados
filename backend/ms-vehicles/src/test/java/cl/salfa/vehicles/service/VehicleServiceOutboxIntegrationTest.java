package cl.salfa.vehicles.service;

import cl.salfa.vehicles.dto.VehicleRequest;
import cl.salfa.vehicles.dto.VehicleResponse;
import cl.salfa.vehicles.entity.VehicleStatus;
import cl.salfa.vehicles.messaging.outbox.OutboxStatus;
import cl.salfa.vehicles.messaging.outbox.VehicleOutboxEvent;
import cl.salfa.vehicles.messaging.outbox.VehicleOutboxRepository;
import cl.salfa.vehicles.repository.VehicleRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class VehicleServiceOutboxIntegrationTest {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private VehicleOutboxRepository outboxRepository;

    @BeforeEach
    void setUp() {
        outboxRepository.deleteAll();
        vehicleRepository.deleteAll();
    }

    private VehicleRequest request(
            String vin,
            String plate,
            String branch
    ) {
        return new VehicleRequest(
                vin,
                plate,
                "Toyota",
                "Corolla",
                "1.8",
                2024,
                15000,
                "Blanco",
                "Gasolina",
                branch,
                VehicleStatus.AVAILABLE
        );
    }

    @Test
    void shouldCreateVehicleAndOutboxEvent() {

        VehicleResponse response = vehicleService.create(
                request(
                        "TESTVIN0000000001",
                        "ABCD11",
                        "SANTIAGO"
                )
        );

        assertNotNull(response.id());
        assertTrue(vehicleRepository.existsById(response.id()));

        List<VehicleOutboxEvent> events =
                outboxRepository.findAll();

        assertEquals(1, events.size());

        VehicleOutboxEvent event = events.getFirst();

        assertEquals(
                "VEHICLE_CREATED",
                event.getEventType()
        );

        assertEquals(response.id(), event.getAggregateId());
        assertEquals(response.vin(), event.getEventKey());
        assertEquals(OutboxStatus.PENDING, event.getStatus());
        assertNotNull(event.getPayload());
    }

    @Test
    void shouldUpdateVehicleAndCreateAnotherEvent() {

        VehicleResponse created = vehicleService.create(
                request(
                        "TESTVIN0000000002",
                        "ABCD12",
                        "SANTIAGO"
                )
        );

        VehicleResponse updated = vehicleService.update(
                created.id(),
                request(
                        "TESTVIN0000000002",
                        "ABCD12",
                        "ANTOFAGASTA"
                )
        );

        assertEquals("ANTOFAGASTA", updated.branch());

        List<VehicleOutboxEvent> events =
                outboxRepository.findAll();

        assertEquals(2, events.size());

        assertEquals(
                1,
                events.stream()
                        .filter(event ->
                                "VEHICLE_CREATED".equals(
                                        event.getEventType()
                                )
                        )
                        .count()
        );

        VehicleOutboxEvent updateEvent =
                events.stream()
                        .filter(event ->
                                "VEHICLE_UPDATED".equals(
                                        event.getEventType()
                                )
                        )
                        .findFirst()
                        .orElseThrow();

        assertEquals(created.id(), updateEvent.getAggregateId());

        assertTrue(
                updateEvent.getPayload().contains("ANTOFAGASTA")
        );
    }

    @Test
    void shouldDeleteVehicleAndPreserveOutboxEvent() {

        VehicleResponse created = vehicleService.create(
                request(
                        "TESTVIN0000000003",
                        "ABCD13",
                        "SANTIAGO"
                )
        );

        vehicleService.delete(created.id());

        assertFalse(
                vehicleRepository.existsById(created.id())
        );

        List<VehicleOutboxEvent> events =
                outboxRepository.findAll();

        assertEquals(2, events.size());

        assertTrue(
                events.stream().anyMatch(event ->
                        "VEHICLE_DELETED".equals(
                                event.getEventType()
                        )
                )
        );
    }

    @Test
    void shouldNormalizeVinAndPlateBeforeCheckingDuplicates() {

        vehicleService.create(
                request(
                        "TESTVIN0000000004",
                        "ABCD14",
                        "SANTIAGO"
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> vehicleService.create(
                        request(
                                "testvin0000000004",
                                "abcd15",
                                "SANTIAGO"
                        )
                )
        );

        assertEquals(1, vehicleRepository.count());
        assertEquals(1, outboxRepository.count());
    }
}
