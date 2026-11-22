package cl.salfa.vehicles.service;

import cl.salfa.vehicles.dto.VehicleRequest;
import cl.salfa.vehicles.entity.VehicleStatus;
import cl.salfa.vehicles.messaging.dto.VehicleEvent;
import cl.salfa.vehicles.messaging.outbox.VehicleOutboxRepository;
import cl.salfa.vehicles.messaging.outbox.VehicleOutboxService;
import cl.salfa.vehicles.repository.VehicleRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@ActiveProfiles("test")
class VehicleServiceRollbackTest {

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private VehicleOutboxRepository outboxRepository;

    @MockitoBean
    private VehicleOutboxService outboxService;

    @BeforeEach
    void setUp() {
        outboxRepository.deleteAll();
        vehicleRepository.deleteAll();
    }

    @Test
    void shouldRollbackVehicleWhenOutboxFails() {

        VehicleRequest request = new VehicleRequest(
                "TESTVIN0000000005",
                "ABCD15",
                "Toyota",
                "Corolla",
                "1.8",
                2024,
                15000,
                "Blanco",
                "Gasolina",
                "SANTIAGO",
                VehicleStatus.AVAILABLE
        );

        doThrow(
                new IllegalStateException(
                        "Fallo simulado del Outbox"
                )
        ).when(outboxService).store(
                any(VehicleEvent.class)
        );

        assertThrows(
                IllegalStateException.class,
                () -> vehicleService.create(request)
        );

        assertEquals(
                0,
                vehicleRepository.count(),
                "El vehículo no debe quedar almacenado"
        );

        assertEquals(
                0,
                outboxRepository.count(),
                "No deben quedar eventos pendientes"
        );
    }


    @Test
    void shouldRollbackVehicleUpdateWhenOutboxFails() {

        VehicleRequest originalRequest = new VehicleRequest(
                "TESTVIN0000000006",
                "ABCD16",
                "Toyota",
                "Corolla",
                "1.8",
                2024,
                15000,
                "Blanco",
                "Gasolina",
                "SANTIAGO",
                VehicleStatus.AVAILABLE
        );

        var created = vehicleService.create(originalRequest);

        VehicleRequest updatedRequest = new VehicleRequest(
                "TESTVIN0000000006",
                "ABCD16",
                "Toyota",
                "Corolla",
                "1.8",
                2024,
                22000,
                "Blanco",
                "Gasolina",
                "ANTOFAGASTA",
                VehicleStatus.WORKSHOP
        );

        doThrow(
                new IllegalStateException(
                        "Fallo simulado del Outbox al actualizar"
                )
        ).when(outboxService).store(
                any(VehicleEvent.class)
        );

        assertThrows(
                IllegalStateException.class,
                () -> vehicleService.update(
                        created.id(),
                        updatedRequest
                )
        );

        var persisted = vehicleRepository
                .findById(created.id())
                .orElseThrow();

        assertEquals("SANTIAGO", persisted.getBranch());
        assertEquals(15000, persisted.getMileage());
        assertEquals(VehicleStatus.AVAILABLE, persisted.getStatus());

        assertEquals(1, vehicleRepository.count());
        assertEquals(0, outboxRepository.count());
    }


    @Test
    void shouldRollbackVehicleDeletionWhenOutboxFails() {

        VehicleRequest request = new VehicleRequest(
                "TESTVIN0000000007",
                "ABCD17",
                "Toyota",
                "Corolla",
                "1.8",
                2024,
                15000,
                "Blanco",
                "Gasolina",
                "SANTIAGO",
                VehicleStatus.AVAILABLE
        );

        var created = vehicleService.create(request);

        doThrow(
                new IllegalStateException(
                        "Fallo simulado del Outbox al eliminar"
                )
        ).when(outboxService).store(
                any(VehicleEvent.class)
        );

        assertThrows(
                IllegalStateException.class,
                () -> vehicleService.delete(created.id())
        );

        assertTrue(
                vehicleRepository.existsById(created.id()),
                "El vehículo debe permanecer después del rollback"
        );

        assertEquals(1, vehicleRepository.count());
        assertEquals(0, outboxRepository.count());
    }

}
