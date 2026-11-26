
CREATE TABLE IF NOT EXISTS vehicle_analytics_events (
                                                        event_id UUID PRIMARY KEY,
                                                        event_type VARCHAR(80) NOT NULL,
    event_version INTEGER NOT NULL,
    vehicle_id BIGINT NOT NULL,
    vin VARCHAR(17) NOT NULL,
    branch VARCHAR(150),
    occurred_at TIMESTAMPTZ NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL
    DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS vehicle_analytics_metrics (
                                                         branch VARCHAR(150) NOT NULL,
    event_type VARCHAR(80) NOT NULL,
    total_events BIGINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL
    DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (branch, event_type)
    );
