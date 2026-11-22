package cl.salfa.vehicles.messaging.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface VehicleOutboxRepository
        extends JpaRepository<VehicleOutboxEvent, UUID> {


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT e
    FROM VehicleOutboxEvent e
    WHERE
        (
            e.status = :pending
            AND (
                e.nextAttemptAt IS NULL
                OR e.nextAttemptAt <= :now
            )
        )
        OR
        (
            e.status = :processing
            AND e.claimedUntil <= :now
        )
    ORDER BY e.createdAt ASC, e.id ASC
    """)
    List<VehicleOutboxEvent> findClaimable(
            @Param("pending") OutboxStatus pending,
            @Param("processing") OutboxStatus processing,
            @Param("now") Instant now,
            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT e
    FROM VehicleOutboxEvent e
    WHERE e.id = :id
    """)
    Optional<VehicleOutboxEvent> findLockedById(
            @Param("id") UUID id
    );

}
