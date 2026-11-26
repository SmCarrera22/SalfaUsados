# Contratos de eventos Salfa360

## Vehicle Events

Topico: vehicle.events

Clave: VIN.

Version inicial del contrato: 1.

Tipos de evento:

- VEHICLE_CREATED
- VEHICLE_UPDATED
- VEHICLE_DELETED

Ejemplo representativo:

```json
{
  "eventId": "9d672a6d-a084-4935-97d6-846a70455c95",
  "eventType": "VEHICLE_CREATED",
  "eventVersion": 1,
  "vehicleId": 11,
  "vin": "JTDBR32E750123456",
  "branch": "Movicenter",
  "occurredAt": "2026-11-25T02:30:09.626030Z"
}
```

Consumidores:

- salfa360-vehicle-history
- salfa360-vehicle-analytics

Retencion: 7 dias.

Dead Letter Topic: vehicle.events.dlq.

## Workshop Events

Topico: workshop.events.

Clave: identificador del vehiculo convertido a String.

Evento implementado:

- WORKSHOP_ORDER_CREATED

Consumidor:

- salfa360-workshop-history

Retencion: 14 dias.

Dead Letter Topic: workshop.events.dlq.

El contrato Java correspondiente se encuentra en
el codigo fuente de ms-workshop.

## Reglas de procesamiento

- Identificadores unicos para eventos.
- Validacion antes de persistencia.
- Confirmacion de offsets despues del procesamiento.
- Idempotencia ante redelivery.
- Reintentos acotados.
- Derivacion a DLQ ante errores no recuperados.

Kafka mantiene el orden dentro de una particion.
No se garantiza orden global entre particiones.

