
#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="${SCRIPT_DIR}/../compose-cluster.yaml"

BROKER="kafka1:29092"

echo "=================================="
echo "SALFA360 - INSPECCION KAFKA"
echo "=================================="

echo ""
echo "===== TOPICOS ====="

docker compose -f "$COMPOSE_FILE" \
  exec -T kafka1 \
  kafka-topics \
  --bootstrap-server "$BROKER" \
  --list

echo ""
echo "===== VEHICLE EVENTS ====="

docker compose -f "$COMPOSE_FILE" \
  exec -T kafka1 \
  kafka-topics \
  --bootstrap-server "$BROKER" \
  --describe \
  --topic vehicle.events

echo ""
echo "===== WORKSHOP EVENTS ====="

docker compose -f "$COMPOSE_FILE" \
  exec -T kafka1 \
  kafka-topics \
  --bootstrap-server "$BROKER" \
  --describe \
  --topic workshop.events

echo ""
echo "===== CONSUMER GROUPS ====="

docker compose -f "$COMPOSE_FILE" \
  exec -T kafka1 \
  kafka-consumer-groups \
  --bootstrap-server "$BROKER" \
  --list

echo ""
echo "===== VEHICLE HISTORY GROUP ====="

docker compose -f "$COMPOSE_FILE" \
  exec -T kafka1 \
  kafka-consumer-groups \
  --bootstrap-server "$BROKER" \
  --describe \
  --group salfa360-vehicle-history
