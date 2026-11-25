package cl.salfa.vehicles.service;

import cl.salfa.vehicles.messaging.dto.VehicleEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

@Service
@RequiredArgsConstructor
public class VehicleAnalyticsService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public boolean register(VehicleEvent event) {

        String branch = event.branch() == null
                || event.branch().isBlank()
                ? "SIN_SUCURSAL"
                : event.branch();

        int inserted = jdbcTemplate.update(
                """
                INSERT INTO vehicle_analytics_events (
                    event_id,
                    event_type,
                    event_version,
                    vehicle_id,
                    vin,
                    branch,
                    occurred_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (event_id) DO NOTHING
                """,
                event.eventId(),
                event.eventType(),
                event.eventVersion(),
                event.vehicleId(),
                event.vin(),
                branch,
                Timestamp.from(event.occurredAt())
        );

        if (inserted == 0) {
            return false;
        }

        jdbcTemplate.update(
                """
                INSERT INTO vehicle_analytics_metrics (
                    branch,
                    event_type,
                    total_events,
                    updated_at
                )
                VALUES (?, ?, 1, CURRENT_TIMESTAMP)
                ON CONFLICT (branch, event_type)
                DO UPDATE SET
                    total_events =
                        vehicle_analytics_metrics.total_events + 1,
                    updated_at = CURRENT_TIMESTAMP
                """,
                branch,
                event.eventType()
        );

        return true;
    }
}
