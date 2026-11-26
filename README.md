# Salfa360

Proyecto academico de Desarrollo Cloud Native - DSY1107.

Plataforma distribuida para la gestion operacional de
vehiculos usados, usuarios, talleres y ordenes de trabajo.

## Version actual

Entrega Parcial 3 - Arquitectura orientada a eventos,
integrando las funcionalidades desarrolladas en EP1 y EP2.

## Funcionalidades

- Gestion de usuarios y control de acceso por roles.
- Gestion del inventario de vehiculos.
- Perfil operacional e historial de eventos por VIN.
- Gestion de talleres y ordenes de trabajo.
- Metricas operacionales por taller.
- Procesamiento asincrono mediante RabbitMQ.
- Streaming de eventos mediante Apache Kafka.
- Administracion de topicos y grupos de consumidores.
- Actualizacion automatica del historial de vehiculos.

## Arquitectura

Frontend React desplegado en Azure App Service.

Autenticacion mediante Microsoft Entra ID, MSAL,
OAuth 2.0, OpenID Connect y JWT.

Flujo de acceso:

React -> API Gateway -> VPC Link -> Internal ALB -> BFF

El BFF valida JWT, scopes y roles, y delega las
operaciones a los microservicios correspondientes.

Los servicios backend se ejecutan en AWS EC2.
La persistencia utiliza PostgreSQL en Neon.

## Componentes

| Componente | Responsabilidad |
|---|---|
| frontend | Interfaz React y autenticacion MSAL |
| bff | Acceso centralizado y seguridad |
| ms-user | Gestion de usuarios |
| ms-vehicles | Inventario, historial y analitica |
| ms-workshop | Talleres y ordenes de trabajo |
| ms-rabbit-admin | Administracion de RabbitMQ |
| ms-kafka-admin | Administracion de Kafka |

## Mensajeria

### RabbitMQ

- Cluster de dos nodos.
- Exchanges direct y topic.
- Tres colas principales y sus respectivas DLQ.
- Procesamiento asincrono de ordenes de taller.
- Confirmacion manual e idempotencia.

### Apache Kafka

Cluster desplegado mediante Docker Compose en AWS EC2:

- Tres nodos ZooKeeper.
- Tres brokers Kafka.
- Kafka UI.
- Tres particiones por topico de negocio.
- Factor de replicacion 3.
- Minimo de replicas sincronizadas: 2.
- Productores con acks=all e idempotencia.

| Topico | Clave | Retencion |
|---|---|---|
| vehicle.events | VIN | 7 dias |
| workshop.events | vehicleId | 14 dias |
| vehicle.events.dlq | Clave original | 14 dias |
| workshop.events.dlq | Clave original | 14 dias |

Grupos de consumidores:

- salfa360-vehicle-history
- salfa360-vehicle-analytics
- salfa360-workshop-history

Se utiliza Transactional Outbox para persistir las
operaciones de negocio y sus eventos asociados.

Los consumidores aplican validacion, idempotencia,
confirmacion de offsets y tratamiento de errores.

## Estructura del repositorio

```text
backend/
  bff/
  ms-user/
  ms-vehicles/
  ms-workshop/
  ms-rabbit-admin/
  ms-kafka-admin/
frontend/
infrastructure/
  database/
  deployment/
  kafka/
  rabbitmq/
docs/
```

Cada componente backend puede compilarse y desplegarse
independientemente.

## Requisitos

- Java 25.
- Gradle Wrapper.
- Node.js y pnpm.
- Docker y Docker Compose.
- Configuracion de Microsoft Entra ID.
- Bases de datos PostgreSQL.

## Compilacion backend

Desde la raiz del repositorio:

```bash
for MODULE in bff ms-user ms-vehicles ms-workshop ms-rabbit-admin ms-kafka-admin
do
  (cd "backend/$MODULE" && ./gradlew clean test bootJar) || break
done
```

## Frontend

```bash
cd frontend
pnpm install
pnpm lint
pnpm build
pnpm dev
```

La configuracion utiliza variables VITE_*.

VITE_API_BASE_URL identifica el API Gateway del ambiente.

Los archivos locales con configuracion sensible no
se incluyen en el repositorio.

## Kafka local

```bash
docker compose -f infrastructure/kafka/compose-cluster.yaml up -d
bash infrastructure/kafka/scripts/create-topics.sh
```

Kafka UI local:

http://localhost:8085

## RabbitMQ local

```bash
docker compose -f infrastructure/rabbitmq/compose.yaml up -d
```

Se requiere configurar previamente las variables del
ambiente y verificar la membresia del cluster.

## Despliegue

- Backend: AWS EC2 y systemd.
- Frontend: Azure App Service.
- CI/CD: GitHub Actions.
- Persistencia: Neon PostgreSQL.

Frontend:

https://salfa360-frontend-aqgtg0adhgaffvaq.westus-01.azurewebsites.net

## Documentacion

- [Arquitectura del sistema](docs/ARQUITECTURA_SALFA360.md)
- [Contratos de eventos](docs/CONTRATOS_EVENTOS.md)
- [Despliegue](infrastructure/deployment/README.md)
- [Kafka Admin](backend/ms-kafka-admin/README.md)
- [Evidencias](docs/evidencias/)
- [Codigo frontend](frontend/)
- [Migraciones PostgreSQL](infrastructure/database/migrations/)
- [Infraestructura Kafka](infrastructure/kafka/)
- [Infraestructura RabbitMQ](infrastructure/rabbitmq/)

## Estrategia Git

feature/* -> dev -> main

La rama main contiene las versiones destinadas a entrega.

## Seguridad y limitaciones

No se versionan credenciales, access tokens, claves SSH
ni archivos de entorno privados.

Kafka y RabbitMQ utilizan multiples nodos dentro de
sus respectivas instancias EC2. Esto permite demostrar
replicacion y distribucion logica, pero no garantiza
disponibilidad ante la perdida completa de una instancia.

Kafka Admin se encuentra restringido a loopback y
su acceso remoto se realiza mediante un tunel SSH.

El proyecto utiliza un monorepositorio con componentes
independientes.

Proyecto desarrollado con fines academicos.
