#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

COMPOSE_FILE="${KAFKA_COMPOSE_FILE:-${SCRIPT_DIR}/../compose-cluster.yaml}"

for TOPIC in \
    vehicle.events \
    workshop.events \
    vehicle.events.dlq \
    workshop.events.dlq
do
    RETENTION_MS=1209600000

    if [[ "$TOPIC" == "vehicle.events" ]]; then
        RETENTION_MS=604800000
    fi

    docker compose -f "$COMPOSE_FILE" \
        exec -T kafka1 kafka-topics \
        --bootstrap-server kafka1:29092 \
        --create \
        --if-not-exists \
        --topic "$TOPIC" \
        --partitions 3 \
        --replication-factor 3 \
        --config min.insync.replicas=2 \
        --config cleanup.policy=delete \
        --config retention.ms="$RETENTION_MS"

    docker compose -f "$COMPOSE_FILE" \
        exec -T kafka1 kafka-configs \
        --bootstrap-server kafka1:29092 \
        --alter \
        --entity-type topics \
        --entity-name "$TOPIC" \
        --add-config "retention.ms=$RETENTION_MS,min.insync.replicas=2,cleanup.policy=delete"
done

echo "Topicos Salfa360 configurados."