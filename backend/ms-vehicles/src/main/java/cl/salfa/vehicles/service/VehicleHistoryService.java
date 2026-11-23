package cl.salfa.vehicles.service;

import cl.salfa.vehicles.entity.VehicleHistoryEvent;
import cl.salfa.vehicles.messaging.dto.VehicleEvent;
import cl.salfa.vehicles.repository.VehicleHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class VehicleHistoryService {

    private final JdbcTemplate jdbcTemplate;
    private final VehicleHistoryRepository repository;

    @Transactional
    public boolean register(VehicleEvent event) {

        String sql = """
                INSERT INTO vehicle_history_events
                    (event_id, vehicle_id, vin,
                     event_type, branch, occurred_at)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT (event_id) DO NOTHING
                """;

        int inserted = jdbcTemplate.update(sql, ps -> {
            ps.setObject(1, event.eventId());
            ps.setLong(2, event.vehicleId());
            ps.setString(3, event.vin());
            ps.setString(4, event.eventType());
            ps.setString(5, event.branch());
            ps.setObject(
                    6,
                    OffsetDateTime.ofInstant(
                            event.occurredAt(),
                            ZoneOffset.UTC
                    )
            );
        });

        return inserted == 1;
    }

    @Transactional(readOnly = true)
    public List<VehicleHistoryEvent> findByVin(String vin) {

        String normalizedVin =
                vin.trim().toUpperCase(Locale.ROOT);

        return repository
                .findByVinOrderByOccurredAtDescEventIdDesc(
                        normalizedVin
                );
    }
}
