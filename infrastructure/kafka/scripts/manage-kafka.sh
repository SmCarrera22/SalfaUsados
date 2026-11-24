
#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="${SCRIPT_DIR}/../compose-cluster.yaml"

BROKER="kafka1:29092"
DEMO_TOPIC="salfa360.admin.demo"

kafka() {
    docker compose -f "$COMPOSE_FILE" \
        exec -T kafka1 "$@"
}

usage() {
    echo "Uso: $0 <comando>"
    echo ""
    echo "Comandos disponibles:"
    echo "  list             Lista los tópicos"
    echo "  create-demo      Crea un tópico de prueba"
    echo "  describe-demo    Inspecciona el tópico de prueba"
    echo "  config-demo      Consulta su configuración"
    echo "  retention-demo   Configura retención de 1 día"
    echo "  groups           Lista los consumer groups"
    echo "  vehicle-group    Inspecciona el grupo de vehículos"
    echo "  delete-demo      Elimina SOLO el tópico de prueba"
    echo "  restore-retention-demo   Restaura retención a 1 día"
}

command="${1:-}"

case "$command" in

    list)
        kafka kafka-topics \
            --bootstrap-server "$BROKER" \
            --list
        ;;

    create-demo)
        kafka kafka-topics \
            --bootstrap-server "$BROKER" \
            --create \
            --if-not-exists \
            --topic "$DEMO_TOPIC" \
            --partitions 3 \
            --replication-factor 3 \
            --config min.insync.replicas=2 \
            --config retention.ms=86400000
        ;;

    describe-demo)
        kafka kafka-topics \
            --bootstrap-server "$BROKER" \
            --describe \
            --topic "$DEMO_TOPIC"
        ;;

    config-demo)
        kafka kafka-configs \
            --bootstrap-server "$BROKER" \
            --entity-type topics \
            --entity-name "$DEMO_TOPIC" \
            --describe
        ;;

    retention-demo)
        kafka kafka-configs \
            --bootstrap-server "$BROKER" \
            --entity-type topics \
            --entity-name "$DEMO_TOPIC" \
            --alter \
            --add-config retention.ms=172800000
        ;;

    restore-retention-demo)
        kafka kafka-configs \
            --bootstrap-server "$BROKER" \
            --entity-type topics \
            --entity-name "$DEMO_TOPIC" \
            --alter \
            --add-config retention.ms=86400000
        ;;

    groups)
        kafka kafka-consumer-groups \
            --bootstrap-server "$BROKER" \
            --list
        ;;

    vehicle-group)
        kafka kafka-consumer-groups \
            --bootstrap-server "$BROKER" \
            --describe \
            --group salfa360-vehicle-history
        ;;

    delete-demo)
        echo "Se eliminará únicamente: $DEMO_TOPIC"
        read -r -p "Escribe ELIMINAR para confirmar: " confirm

        if [[ "$confirm" != "ELIMINAR" ]]; then
            echo "Operación cancelada."
            exit 0
        fi

        kafka kafka-topics \
            --bootstrap-server "$BROKER" \
            --delete \
            --topic "$DEMO_TOPIC"
        ;;

    *)
        usage
        exit 1
        ;;
esac
