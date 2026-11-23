package cl.salfa.vehicles.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "vehicle_history_events",
        indexes = {
                @Index(
                        name = "idx_vehicle_history_vin_date",
                        columnList = "vin, occurred_at"
                )
        }
)
@Getter
@NoArgsConstructor
public class VehicleHistoryEvent {

    @Id
    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(nullable = false, length = 17)
    private String vin;

    @Column(name = "event_type", nullable = false, length = 80)
    private String eventType;

    @Column(name = "branch")
    private String branch;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;
}
