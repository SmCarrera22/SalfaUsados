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
        --config retention.ms=604800000
done

echo "Topicos Salfa360 configurados."