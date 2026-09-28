# Salfa360

Plataforma web para la gestión centralizada de vehículos usados, desarrollada como proyecto académico para la asignatura **Desarrollo Cloud Native**.

Salfa360 busca centralizar la información operacional del inventario de vehículos usados, reemplazando procesos fragmentados y permitiendo administrar vehículos y usuarios desde una aplicación web con autenticación centralizada, control de acceso basado en roles y una arquitectura distribuida desplegada sobre servicios cloud.

---

## 1. Descripción del proyecto

Salfa360 nace como una propuesta para centralizar la gestión operacional de vehículos usados.

La solución permite:

- Consultar el inventario de vehículos.
- Registrar nuevos vehículos.
- Modificar información de vehículos existentes.
- Eliminar vehículos según permisos.
- Consultar usuarios del sistema.
- Restringir funcionalidades según el rol del usuario.
- Autenticar usuarios mediante Microsoft Entra ID.
- Proteger las APIs mediante OAuth 2.0, OpenID Connect y JWT.
- Persistir la información en bases de datos PostgreSQL independientes por microservicio.

La solución utiliza una arquitectura basada en **Frontend + BFF + Microservicios**, incorporando servicios de Microsoft Azure y Amazon Web Services.

---

## 2. Arquitectura general

El proyecto se encuentra organizado y contiene componentes independientes para frontend, BFF y microservicios.

```text
SalfaUsados/
├── backend/
│   ├── bff/
│   ├── ms-user/
│   └── ms-vehicles/
│
└── frontend/
```

Cada aplicación backend puede compilarse y desplegarse independientemente.

### Componentes

| Componente | Tecnología | Responsabilidad |
|---|---|---|
| Frontend | React + Vite | Interfaz de usuario y autenticación mediante MSAL |
| BFF | Spring Boot | Punto de acceso del frontend, validación JWT, RBAC y comunicación con microservicios |
| ms-user | Spring Boot | Gestión de usuarios de negocio |
| ms-vehicles | Spring Boot | Gestión del inventario de vehículos |
| Microsoft Entra ID | OAuth 2.0 / OIDC | Identidad, autenticación y roles |
| AWS API Gateway | HTTP API | Punto público de entrada al backend y validación JWT |
| Application Load Balancer | AWS ALB | Enrutamiento privado hacia el BFF |
| Amazon EC2 | AWS | Ejecución del BFF y microservicios |
| Neon | PostgreSQL | Persistencia independiente de usuarios y vehículos |

---

## 3. Flujo de funcionamiento

El flujo principal de una solicitud autenticada es el siguiente:

```text
┌───────────────────┐
│      Usuario      │
└─────────┬─────────┘
          │
          ▼
┌───────────────────────────┐
│ React + Vite              │
│ Frontend                  │
└────────────┬──────────────┘
             │
             │ Login
             ▼
┌───────────────────────────┐
│ Microsoft Entra ID        │
│ OAuth 2.0 / OpenID Connect│
└────────────┬──────────────┘
             │
             │ Access Token JWT
             ▼
┌───────────────────────────┐
│ React + MSAL              │
│ Authorization: Bearer JWT │
└────────────┬──────────────┘
             │
             │ HTTPS
             ▼
┌───────────────────────────┐
│ AWS API Gateway           │
│ JWT Authorizer            │
│ CORS                      │
└────────────┬──────────────┘
             │
             │ VPC Link
             ▼
┌───────────────────────────┐
│ Internal ALB              │
└────────────┬──────────────┘
             │
             ▼
┌───────────────────────────┐
│ BFF - Spring Boot         │
│ JWT Validation            │
│ Scope Validation          │
│ Role Based Access Control │
└───────┬───────────┬───────┘
        │           │
        ▼           ▼
┌──────────────┐  ┌──────────────────┐
│   ms-user    │  │   ms-vehicles    │
│ Spring Boot  │  │   Spring Boot    │
└──────┬───────┘  └────────┬─────────┘
       │                   │
       ▼                   ▼
┌──────────────┐  ┌──────────────────┐
│ PostgreSQL   │  │ PostgreSQL       │
│ Users - Neon │  │ Vehicles - Neon  │
└──────────────┘  └──────────────────┘
```

### Flujo de autenticación y autorización

1. El usuario accede al frontend.
2. React utiliza **MSAL** para iniciar el proceso de autenticación.
3. El usuario se autentica mediante **Microsoft Entra ID**.
4. Entra ID entrega un Access Token JWT al frontend.
5. El frontend incorpora el token en las solicitudes mediante el encabezado:

```http
Authorization: Bearer <access_token>
```

6. La solicitud llega a **AWS API Gateway**.
7. El JWT Authorizer valida el token antes de permitir el acceso al backend.
8. API Gateway envía la solicitud mediante un **VPC Link** hacia el Application Load Balancer interno.
9. El ALB dirige la solicitud hacia el BFF.
10. El BFF realiza una segunda validación del JWT.
11. Spring Security aplica autorización basada en scopes y roles.
12. El BFF deriva la operación al microservicio correspondiente.
13. El microservicio ejecuta la lógica de negocio y consulta su propia base de datos.
14. La respuesta regresa por la misma cadena hasta el frontend.

De esta forma, ocultar una funcionalidad en la interfaz no constituye el mecanismo de seguridad: la autorización efectiva también es aplicada en el backend.

---

## 4. Arquitectura Cloud

La infraestructura implementada utiliza AWS como plataforma principal de ejecución y Microsoft Entra ID como proveedor de identidad.

```text
                         INTERNET
                            │
                ┌───────────┴────────────┐
                │                        │
                ▼                        ▼
      ┌──────────────────┐      ┌─────────────────────┐
      │ React Frontend   │      │ Microsoft Entra ID  │
      │ localhost:5173   │◄────►│ OAuth2 / OIDC       │
      └────────┬─────────┘      └─────────────────────┘
               │
               │ HTTPS + Bearer JWT
               ▼
      ┌─────────────────────────┐
      │ AWS API Gateway         │
      │ HTTP API                │
      │ JWT Authorizer + CORS   │
      └────────────┬────────────┘
                   │
                   │ VPC Link
                   ▼
┌─────────────────────────────────────────────────────────┐
│                    salfa360-vpc                         │
│                    10.0.0.0/16                         │
│                                                         │
│   ┌─────────────────────────────────────────────────┐   │
│   │ Internal Application Load Balancer              │   │
│   └───────────────────────┬─────────────────────────┘   │
│                           │                             │
│                           ▼                             │
│                 ┌───────────────────┐                   │
│                 │ EC2 #1            │                   │
│                 │ BFF               │                   │
│                 │ Spring Boot :8080 │                   │
│                 └─────────┬─────────┘                   │
│                           │                             │
│                 ┌─────────┴─────────┐                   │
│                 │                   │                   │
│                 ▼                   ▼                   │
│       ┌─────────────────┐  ┌─────────────────────┐      │
│       │ EC2 #2          │  │ EC2 #3              │      │
│       │ ms-user         │  │ ms-vehicles         │      │
│       │ :8081           │  │ :8082               │      │
│       └────────┬────────┘  └──────────┬──────────┘      │
│                │                      │                 │
└────────────────┼──────────────────────┼─────────────────┘
                 │                      │
                 ▼                      ▼
       ┌─────────────────┐    ┌───────────────────┐
       │ Neon PostgreSQL │    │ Neon PostgreSQL   │
       │ Users Database  │    │ Vehicles Database │
       └─────────────────┘    └───────────────────┘
```

### Comunicación interna

Los servicios backend no exponen directamente sus puertos de aplicación a Internet.

El flujo hacia el BFF es:

```text
API Gateway
     │
     ▼
VPC Link
     │
     ▼
Internal ALB
     │
     ▼
BFF :8080
```

El BFF se comunica internamente con:

```text
BFF
 ├──► ms-user :8081
 └──► ms-vehicles :8082
```

Los Security Groups restringen la comunicación entre los distintos componentes.

---

## 5. Microservicios

### BFF

Responsabilidades principales:

- Recibir las solicitudes provenientes del frontend.
- Validar JWT emitidos por Microsoft Entra ID.
- Validar el scope requerido.
- Aplicar autorización basada en roles.
- Comunicarse con los microservicios internos.
- Centralizar el acceso del frontend al dominio backend.

El BFF no posee una base de datos propia.

---

### ms-user

Microservicio responsable de administrar la información de negocio de los usuarios.

La autenticación no es responsabilidad de este servicio. La identidad y las credenciales son administradas por Microsoft Entra ID.

Persistencia:

```text
ms-user
   │
   ▼
Neon PostgreSQL
Users Database
```

---

### ms-vehicles

Microservicio responsable de la gestión del inventario de vehículos.

Permite operaciones de:

- Consulta.
- Creación.
- Actualización.
- Eliminación.
- Validación de datos.
- Control de duplicidad de VIN y patente.

Persistencia:

```text
ms-vehicles
      │
      ▼
Neon PostgreSQL
Vehicles Database
```

---

## 6. Autenticación y autorización

La autenticación utiliza:

- Microsoft Entra ID
- Microsoft Authentication Library (MSAL)
- OAuth 2.0
- OpenID Connect
- JSON Web Tokens (JWT)

El API expone el scope:

```text
access_as_user
```

### Roles

Salfa360 implementa tres roles:

| Rol | Vehículos | Usuarios |
|---|---|---|
| ADMIN | Crear, consultar, editar y eliminar | Consultar y administrar |
| OPERATOR | Crear, consultar y editar | Sin acceso |
| ANALYST | Solo lectura | Sin acceso |

La autorización se aplica tanto visualmente en el frontend como efectivamente en el backend mediante Spring Security.

---

## 7. Seguridad

La solución incorpora diferentes capas de seguridad:

```text
Microsoft Entra ID
        │
        ▼
Access Token JWT
        │
        ▼
API Gateway JWT Authorizer
        │
        ▼
BFF
JWT + Scope + Roles
        │
        ▼
Microservicios internos
```

Entre las validaciones realizadas se encuentran:

- Solicitud sin token → `401 Unauthorized`.
- Token inválido → `401 Unauthorized`.
- Usuario autenticado sin permisos → `403 Forbidden`.
- Usuario autorizado → operación permitida.
- Validaciones de reglas de negocio → `400 Bad Request` cuando corresponde.

---

## 8. Tecnologías utilizadas

### Frontend

- React
- Vite
- JavaScript
- MSAL
- pnpm

### Backend

- Java 25
- Spring Boot
- Spring Security
- OAuth2 Resource Server
- Spring Data JPA
- Hibernate
- Gradle

### Cloud e infraestructura

- Amazon EC2
- Amazon API Gateway
- AWS VPC
- AWS VPC Link
- Application Load Balancer
- Security Groups
- Microsoft Entra ID
- Neon PostgreSQL

### Control de versiones

- Git
- GitHub
- Feature branches
- Rama de integración `dev`
- Rama estable `main`

---

## 9. Configuración por ambientes

Los proyectos backend separan la configuración según el ambiente:

```text
application.properties
        │
        ├── configuración común
        │
application-local.properties
        ├── desarrollo local
        │
application-test.properties
        ├── pruebas automatizadas
        │
application-prod.properties
        └── producción
```

Los archivos que contienen configuración local sensible no se versionan.

La configuración productiva utiliza variables de entorno, entre ellas:

```text
DB_URL
DB_USERNAME
DB_PASSWORD

MS_USER_URL
MS_VEHICLES_URL

ENTRA_ISSUER_URI
ENTRA_AUDIENCE
ENTRA_REQUIRED_SCOPE
```

No se almacenan credenciales productivas dentro del repositorio.

---

## 10. Ejecución local

### Requisitos

- Java 25
- Node.js
- pnpm
- Gradle Wrapper incluido en cada proyecto backend

### Backend

Cada servicio puede ejecutarse independientemente.

#### ms-user

```bash
cd backend/ms-user
./gradlew bootRun --args='--spring.profiles.active=local'
```

#### ms-vehicles

```bash
cd backend/ms-vehicles
./gradlew bootRun --args='--spring.profiles.active=local'
```

#### BFF

```bash
cd backend/bff
./gradlew bootRun --args='--spring.profiles.active=local'
```

### Frontend

```bash
cd frontend
pnpm install
pnpm dev
```

El frontend se encuentra disponible durante desarrollo en:

```text
http://localhost:5173
```

---

## 11. Compilación y pruebas

### Backend

Ejecutar dentro de cada proyecto:

```bash
./gradlew clean build
```

Los tests utilizan un perfil independiente:

```text
test
```

Los microservicios con persistencia utilizan H2 durante las pruebas para evitar dependencia de las bases de datos productivas.

El BFF utiliza un `JwtDecoder` simulado durante la prueba de contexto para evitar depender de Microsoft Entra ID durante el build.

### Frontend

```bash
cd frontend
pnpm build
```

---

## 12. Estrategia Git

El desarrollo se realizó utilizando ramas por funcionalidad:

```text
feature/vehicles
feature/users
feature/bff
feature/auth
feature/frontend
feature/aws-deployment
```

El flujo de integración utilizado fue:

```text
feature/*
    │
    ▼
   dev
    │
    ▼
   main
```

La versión correspondiente a la Entrega Parcial 1 se identifica mediante el tag:

```text
v1.0.0-ep1
```

---

## 13. Despliegue

Los componentes backend se empaquetan como aplicaciones Spring Boot ejecutables:

```text
bff.jar
ms-user.jar
ms-vehicles.jar
```

Cada componente se ejecuta en una instancia EC2 independiente mediante `systemd`.

```text
EC2 #1 → BFF
EC2 #2 → ms-user
EC2 #3 → ms-vehicles
```

La versión desplegada para la EP1 corresponde al código integrado en la rama `main`.

---

## 14. Limitaciones del entorno académico

El frontend fue preparado para despliegue estático en Amazon S3.

Sin embargo, Amazon S3 Static Website Hosting entrega un endpoint HTTP, mientras que Microsoft Entra ID exige HTTPS para los Redirect URI de aplicaciones SPA fuera de `localhost`.

La solución prevista era utilizar Amazon CloudFront delante de S3 para proporcionar un endpoint HTTPS. El rol IAM disponible en el entorno AWS Academy no cuenta con permisos para crear distribuciones CloudFront (`cloudfront:CreateDistribution`).

Por esta razón, durante la demostración académica:

```text
Frontend → localhost:5173
Backend  → AWS
```

El backend sí se encuentra desplegado sobre la infraestructura AWS implementada.

---

## 15. Versión de entrega

**Entrega Parcial 1**

```text
v1.0.0-ep1
```

La versión fue:

- Integrada en `main`.
- Compilada satisfactoriamente.
- Validada mediante pruebas.
- Desplegada en las instancias EC2 correspondientes.
- Verificada mediante pruebas funcionales y de autorización.