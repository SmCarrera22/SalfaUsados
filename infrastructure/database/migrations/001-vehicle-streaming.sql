
BEGIN;

CREATE TABLE IF NOT EXISTS public.vehicle_outbox_events (
                                                            id UUID PRIMARY KEY,
                                                            aggregate_id BIGINT NOT NULL,
                                                            aggregate_type VARCHAR(50) NOT NULL,
    attempts INTEGER NOT NULL,
    claim_token UUID,
    claimed_until TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    event_key VARCHAR(17) NOT NULL,
    event_type VARCHAR(80) NOT NULL,
    event_version INTEGER NOT NULL,
    last_error TEXT,
    next_attempt_at TIMESTAMPTZ,
    payload TEXT NOT NULL,
    published_at TIMESTAMPTZ,
    status VARCHAR(20) NOT NULL,

    CONSTRAINT vehicle_outbox_events_status_check
    CHECK (
              status IN (
              'PENDING',
              'PROCESSING',
              'PUBLISHED',
              'FAILED'
                        )
    )
    );

CREATE INDEX IF NOT EXISTS idx_vehicle_outbox_aggregate
    ON public.vehicle_outbox_events (
    aggregate_id,
    created_at
    );

CREATE INDEX IF NOT EXISTS idx_vehicle_outbox_pending
    ON public.vehicle_outbox_events (
    status,
    next_attempt_at
    );

CREATE TABLE IF NOT EXISTS public.vehicle_history_events (
                                                             event_id UUID PRIMARY KEY,
                                                             branch VARCHAR(255),
    event_type VARCHAR(80) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    vehicle_id BIGINT NOT NULL,
    vin VARCHAR(17) NOT NULL
    );

CREATE INDEX IF NOT EXISTS idx_vehicle_history_vin_date
    ON public.vehicle_history_events (
    vin,
    occurred_at
    );

CREATE TABLE IF NOT EXISTS public.vehicle_analytics_events (
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

CREATE TABLE IF NOT EXISTS public.vehicle_analytics_metrics (
                                                                branch VARCHAR(150) NOT NULL,
    event_type VARCHAR(80) NOT NULL,
    total_events BIGINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL
    DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (branch, event_type)
    );

COMMIT;
