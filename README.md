# PulsePass

PulsePass es una API REST académica para consultar eventos, artistas y venues, registrar usuarios y gestionar tickets. Implementa las capas de persistencia, servicios y controladores con Java 21, Spring Boot 4, Spring MVC, Spring Data JPA, PostgreSQL y Flyway.

## Stack tecnológico

- Java 21
- Spring Boot 4.1.1
- Spring MVC y Bean Validation
- Spring Data JPA e Hibernate
- PostgreSQL y Flyway
- Maven
- JUnit 5, Mockito y MockMvc
- Testcontainers para pruebas de integración de persistencia

## Alcance

La API expone operaciones sobre venues, eventos, artistas, usuarios y tickets. Los Controllers delegan reglas de negocio en los Services, reciben y devuelven DTOs, y usan un handler global para responder errores con un formato uniforme.

No incluye frontend, autenticación/autorización, pasarela de pagos, notificaciones ni generación de códigos QR.

## Endpoints

Todas las rutas usan el prefijo `/api` y aceptan o responden JSON cuando corresponde.

### Venues

| Método | Ruta | Respuesta correcta |
|---|---|---|
| `GET` | `/api/venues/{code}` | `200 OK` |
| `GET` | `/api/venues/active` | `200 OK` |

### Eventos

| Método | Ruta | Respuesta correcta |
|---|---|---|
| `POST` | `/api/events` | `201 Created` |
| `GET` | `/api/events/{eventCode}` | `200 OK` |
| `GET` | `/api/events/published` | `200 OK` |
| `PATCH` | `/api/events/{eventCode}/publish` | `200 OK` |
| `POST` | `/api/events/{eventCode}/artists/{artistId}` | `200 OK` |
| `GET` | `/api/events/by-artist?stageName={stageName}` | `200 OK` |

### Artistas

| Método | Ruta | Respuesta correcta |
|---|---|---|
| `GET` | `/api/artists/{id}` | `200 OK` |
| `GET` | `/api/artists/by-stage-name?stageName={stageName}` | `200 OK` |
| `GET` | `/api/artists/active` | `200 OK` |

### Usuarios

| Método | Ruta | Respuesta correcta |
|---|---|---|
| `POST` | `/api/users` | `201 Created` |
| `GET` | `/api/users/by-email?email={email}` | `200 OK` |
| `GET` | `/api/users/by-username?username={username}` | `200 OK` |

### Tickets

| Método | Ruta | Respuesta correcta |
|---|---|---|
| `POST` | `/api/tickets` | `201 Created` |
| `GET` | `/api/tickets/{ticketCode}` | `200 OK` |
| `GET` | `/api/tickets/by-user?email={email}` | `200 OK` |
| `GET` | `/api/events/{eventCode}/tickets/paid` | `200 OK` |
| `PATCH` | `/api/tickets/{ticketCode}/cancel` | `200 OK` |
| `PATCH` | `/api/tickets/{ticketCode}/use` | `200 OK` |

Las creaciones validan el request antes de invocar al Service. Los errores se responden con `ErrorResponse`, que contiene `timestamp`, `status`, `error`, `message` y `details`. Los códigos principales son `400` para request inválido, `404` para recurso inexistente, `409` para conflictos de negocio y `500` para errores inesperados.

## Ejemplos de requests

Crear un evento:

```json
{
	"eventCode": "CMF-2026",
	"name": "Caribbean Music Fest 2026",
	"description": "Festival de música del Caribe",
	"category": "MUSIC",
	"eventDate": "2026-11-01T19:00:00",
	"minimumAge": 18,
	"venueCode": "VEN-SMR-01"
}
```

Registrar un usuario:

```json
{
	"username": "andrea",
	"email": "andrea@example.com",
	"firstName": "Andrea",
	"lastName": "Lopez",
	"phone": "3001234567",
	"city": "Santa Marta",
	"birthDate": "2000-01-01"
}
```

Comprar un ticket:

```json
{
	"userEmail": "andrea@example.com",
	"eventCode": "CMF-2026",
	"type": "GENERAL"
}
```

## Requisitos previos

- JDK 21 instalado y `JAVA_HOME` configurado.
- Docker Desktop o Docker Engine activo para las pruebas de integración que usan Testcontainers.
- Para ejecutar la aplicación, PostgreSQL debe estar disponible con la configuración indicada abajo.

## Base de datos

La configuración predeterminada está en `src/main/resources/application.properties`:

- URL: `jdbc:postgresql://localhost:5432/pulsepass`
- Usuario: `postgres`
- Contraseña: `postgres`

Para levantar PostgreSQL localmente con Docker:

```bash
docker run --name pulsepass-db -e POSTGRES_DB=pulsepass -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:16
```

Las migraciones de Flyway están en `src/main/resources/db/migration/` y se aplican al iniciar la aplicación y durante las pruebas que configuran la base de datos.

## Ejecutar la aplicación

Desde la raíz del proyecto, en macOS/Linux:

```bash
./mvnw spring-boot:run
```

En Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

La aplicación queda disponible en `http://localhost:8080`.

## Ejecutar las pruebas

Ejecutar la suite completa:

```bash
./mvnw clean test
```

En Windows PowerShell:

```powershell
.\mvnw.cmd clean test
```

Ejecutar únicamente las pruebas de Controllers, validación de DTOs y errores globales:

```bash
./mvnw "-Dtest=*ControllerTest,GlobalExceptionHandlerTest,RequestDtoValidationTest" test
```

En Windows PowerShell:

```powershell
.\mvnw.cmd "-Dtest=*ControllerTest,GlobalExceptionHandlerTest,RequestDtoValidationTest" test
```

Las pruebas de Controllers usan `@WebMvcTest`, MockMvc y Services simulados; no requieren PostgreSQL. La suite completa puede requerir Docker para las pruebas de persistencia con Testcontainers.

## Estructura principal

```text
src/main/java/com/pulsepass/pulsepass/
├── controller/
├── domain/
├── dto/request/
├── dto/response/
├── exception/
├── mapper/
├── repository/
└── service/

src/test/java/com/pulsepass/pulsepass/
├── controller/
├── dto/request/
├── exception/
├── service/
└── ...
```

## Referencias

- PRD de controladores: `PRD_PulsePass_Capa_Controladores_v2.md`
- Código: `src/main/java/com/pulsepass/pulsepass`
- Pruebas: `src/test/java/com/pulsepass/pulsepass`
- Repositorio: <https://github.com/nromero-4/pulsepass>
