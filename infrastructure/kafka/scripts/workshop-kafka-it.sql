
-- SALFA360
-- Esquema auxiliar para las pruebas Kafka de talleres.
-- Ejecutar solamente en salfa360_workshop_kafka_it.

CREATE TABLE IF NOT EXISTS workshop_processed_commands (
                                                           event_id UUID PRIMARY KEY,
                                                           order_id BIGINT,
                                                           processed_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS workshop_kafka_outbox (
                                                     event_id UUID PRIMARY KEY,
                                                     order_id BIGINT NOT NULL,
                                                     event_key VARCHAR(64) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempts INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    claimed_until TIMESTAMPTZ,
    claim_token UUID,
    published_at TIMESTAMPTZ,
    last_error TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_workshop_kafka_outbox_pending
    ON workshop_kafka_outbox(status, next_attempt_at);

CREATE TABLE IF NOT EXISTS workshop_kafka_consumed_events (
                                                              event_id UUID PRIMARY KEY,
                                                              order_id BIGINT NOT NULL,
                                                              vehicle_id BIGINT NOT NULL,
                                                              workshop_id BIGINT NOT NULL,
                                                              event_type VARCHAR(100) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
    );
