package cl.salfa.vehicles.messaging.outbox;

import cl.salfa.vehicles.messaging.dto.VehicleEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class VehicleOutboxServiceTest {

    @Autowired
    private VehicleOutboxService outboxService;

    @Autowired
    private VehicleOutboxRepository repository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    private VehicleEvent createEvent(UUID eventId) {
        return new VehicleEvent(
                eventId,
                "VEHICLE_CREATED",
                1,
                100L,
                "TESTVIN0000000001",
                "SANTIAGO",
                Instant.now()
        );
    }

    @Test
    void shouldStorePendingOutboxEvent() {

        UUID eventId = UUID.randomUUID();

        outboxService.store(createEvent(eventId));

        VehicleOutboxEvent saved =
                repository.findById(eventId).orElseThrow();

        assertEquals(eventId, saved.getId());
        assertEquals(100L, saved.getAggregateId());
        assertEquals("VEHICLE", saved.getAggregateType());
        assertEquals("VEHICLE_CREATED", saved.getEventType());
        assertEquals(1, saved.getEventVersion());

        assertEquals(
                "TESTVIN0000000001",
                saved.getEventKey()
        );

        assertEquals(
                OutboxStatus.PENDING,
                saved.getStatus()
        );

        assertEquals(0, saved.getAttempts());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getNextAttemptAt());

        assertTrue(
                saved.getPayload().contains(
                        eventId.toString()
                )
        );
    }

    @Test
    void shouldRollbackOutboxWhenTransactionFails() {

        UUID eventId = UUID.randomUUID();

        assertThrows(
                IllegalStateException.class,
                () -> transactionTemplate.executeWithoutResult(
                        status -> {

                            outboxService.store(
                                    createEvent(eventId)
                            );

                            throw new IllegalStateException(
                                    "Simulacion de fallo"
                            );
                        }
                )
        );

        assertFalse(
                repository.existsById(eventId),
                "El evento no debe persistir despues del rollback"
        );
    }

    @Test
    void shouldRejectInvalidVin() {

        UUID eventId = UUID.randomUUID();

        VehicleEvent invalidEvent = new VehicleEvent(
                eventId,
                "VEHICLE_CREATED",
                1,
                100L,
                "INVALID",
                "SANTIAGO",
                Instant.now()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> outboxService.store(invalidEvent)
        );

        assertFalse(repository.existsById(eventId));
    }
}
