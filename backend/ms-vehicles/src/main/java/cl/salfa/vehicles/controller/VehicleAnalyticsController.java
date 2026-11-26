package cl.salfa.vehicles.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vehicles/analytics")
@ConditionalOnProperty(
        name = "salfa.kafka.analytics.enabled",
        havingValue = "true"
)
public class VehicleAnalyticsController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public List<Map<String, Object>> findMetrics() {

        return jdbcTemplate.queryForList(
                """
                SELECT
                    branch,
                    event_type,
                    total_events,
                    updated_at
                FROM vehicle_analytics_metrics
                ORDER BY branch, event_type
                """
        );
    }
}
