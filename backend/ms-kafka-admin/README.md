# Salfa360 - MS Kafka Admin

Microservicio administrativo independiente desarrollado
con Java 25, Spring Boot y Apache Kafka AdminClient.

## Responsabilidades

- Listar topicos Kafka.
- Consultar particiones, lideres, replicas e ISR.
- Consultar politicas de retencion y limpieza.
- Crear topicos administrativos.
- Modificar configuraciones de retencion.
- Eliminar topicos administrativos.
- Listar consumer groups.
- Consultar offsets, miembros y lag.

## Compilacion

./gradlew clean test bootJar

## Ejecucion local

./gradlew bootRun

El servicio escucha por defecto en 127.0.0.1:8084.

Los brokers se configuran mediante la variable
KAFKA_BOOTSTRAP_SERVERS.

## Endpoints

| Metodo | Ruta | Operacion |
|---|---|---|
| GET | /api/kafka/topics | Listar topicos |
| GET | /api/kafka/topics/{name} | Consultar topico |
| POST | /api/kafka/topics | Crear topico |
| PATCH | /api/kafka/topics/{name}/retention | Modificar retencion |
| DELETE | /api/kafka/topics/{name} | Eliminar topico |
| GET | /api/kafka/groups | Listar grupos |
| GET | /api/kafka/groups/{groupId} | Consultar offsets y lag |

## Restricciones administrativas

Las operaciones de escritura se restringen a topicos
con prefijo salfa360.admin.

Los topicos administrativos se crean con tres particiones,
factor de replicacion tres y minimo de dos replicas
sincronizadas.

El servicio no implementa autenticacion HTTP propia.
Por este motivo se limita a loopback y su acceso remoto
se realiza exclusivamente mediante un tunel SSH.

No se expone directamente mediante API Gateway.

## Arquitectura

KafkaAdminController -> KafkaAdminService -> Kafka AdminClient

No requiere persistencia propia ni dependencias JPA.

## Consideraciones

Los offsets no confirmados se representan como null.

El administrador utiliza operaciones compatibles con la
version de Kafka empleada por el proyecto. Algunos metodos
de consulta de grupos generan advertencias de deprecacion
y se mantienen por estabilidad de la entrega.
