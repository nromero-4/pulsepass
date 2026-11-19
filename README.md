# PulsePass

PulsePass es una plataforma académica para consultar eventos, artistas y sedes y gestionar entradas. La API REST usa Java 21, Spring Boot y PostgreSQL. El cliente web de la Fase 5 está en [`frontend/`](./frontend).

## Desarrollo local con Docker Compose

Abre Docker Desktop y ejecuta los comandos desde la carpeta que contiene `pom.xml`. El archivo [`compose.yaml`](./compose.yaml) agrupa tres servicios bajo `pulsepass`. Los puertos publicados escuchan en `127.0.0.1`.

| Servicio | Imagen | Puerto en Windows → puerto en Docker |
| --- | --- | --- |
| `database` | `postgres:16-alpine` | `5432` → `5432` |
| `backend` | `pulsepass-backend:local`, construida con el [Dockerfile](./Dockerfile) | `8080` → `8080` |
| `pgadmin` | `dpage/pgadmin4:9.19` | `5050` → `80` |

1. Copia [`.env.example`](./.env.example) a `.env` en la raíz y completa `POSTGRES_PASSWORD`, `PGADMIN_DEFAULT_EMAIL` y `PGADMIN_DEFAULT_PASSWORD`. Elige una contraseña para PostgreSQL y otra para entrar a pgAdmin. Para generar el valor de `PULSEPASS_JWT_SECRET_BASE64`, ejecuta esto en PowerShell y pega el resultado en `.env`.

   ```powershell
   $jwtKey = [byte[]]::new(32)
   [System.Security.Cryptography.RandomNumberGenerator]::Fill($jwtKey)
   [Convert]::ToBase64String($jwtKey)
   ```

2. Valida la configuración y levanta los tres servicios. Compose compila el backend con Maven y Java 21. Después de que la base esté lista, Spring Boot aplica las migraciones de Flyway.

   ```powershell
   docker compose config --quiet
   docker compose up -d --build
   docker compose ps
   docker compose logs -f backend
   ```

   Cuando el backend termine de arrancar, sal de los logs con `Ctrl+C` y prueba la API.

   ```powershell
   Invoke-RestMethod 'http://127.0.0.1:8080/api/venues/active'
   ```

   Una respuesta vacía es normal hasta que cargues sedes. Si prefieres iniciar un servicio a la vez, usa `docker compose up -d database`, luego `docker compose up -d --build backend` y por último `docker compose up -d pgadmin`.

3. Abre [pgAdmin](http://127.0.0.1:5050) con el correo y la contraseña de `PGADMIN_DEFAULT_*`. En **Servers → Register → Server**, escribe `PulsePass` como nombre. En **Connection**, usa el host `database`, el puerto `5432`, la base de mantenimiento `pulsepass` y las credenciales `POSTGRES_USER` y `POSTGRES_PASSWORD` de `.env`. Si cambiaste `POSTGRES_DB`, usa ese nombre. `database` es el nombre que los contenedores utilizan en la red de Compose.

4. Arranca el frontend en Windows con los comandos de la sección siguiente. Para apuntar al puerto de Docker, configura `PULSEPASS_API_TARGET=http://127.0.0.1:8080` en `frontend/.env`. Si otro proyecto ya usa un puerto, cambia el valor `PULSEPASS_*_PORT` correspondiente en el `.env` de la raíz. Si cambias `PULSEPASS_BACKEND_PORT`, usa ese mismo puerto en la prueba de la API y en `PULSEPASS_API_TARGET`.

Para cargar los datos de demostración en la base de Compose, después de que arranque el backend:

```powershell
Get-Content -Raw -LiteralPath 'tests/e2e/fixtures/seed-e2e.sql' |
  docker compose exec -T database psql -v ON_ERROR_STOP=1 -U postgres -d pulsepass
```

Usa tus valores de usuario y base si los cambiaste. `docker compose down` detiene y retira los contenedores del proyecto y conserva los volúmenes de PostgreSQL y pgAdmin.

## Frontend de la Fase 5

Se necesita Node.js 22.22 o superior y una instancia de la API en ejecución. Puedes levantarla con Docker Compose o ejecutar PostgreSQL y Spring Boot según las secciones **Base de datos**, **Configuración JWT** y **Ejecutar la aplicación**. En otra terminal, desde la raíz del proyecto:

```powershell
cd frontend
npm.cmd ci
npm.cmd run dev
```

Abre [http://localhost:5173](http://localhost:5173) o la dirección que indique Vite. En macOS y Linux, usa `npm` en los mismos comandos. Vite redirige las llamadas `/api` a `http://localhost:8080` por defecto. Si el backend usa otro puerto, copia [`frontend/.env.example`](./frontend/.env.example) a `frontend/.env` y ajusta `PULSEPASS_API_TARGET`. La variable solo configura el proxy de Vite. Para servir el resultado de `build` con otro servidor, dirige `/api` a Spring Boot en el mismo origen y sirve `index.html` para las rutas de la aplicación.

| Acceso | Rutas principales | Acciones |
| --- | --- | --- |
| Visitante | `/`, `/eventos`, `/artistas`, `/sedes`, `/login`, `/registro` | Explorar el catálogo y crear una cuenta |
| USER | `/cuenta`, `/eventos/:eventCode`, `/mis-tickets` | Comprar, consultar y cancelar entradas propias |
| STAFF | `/staff/validacion`, `/staff/eventos` | Consultar entradas pagadas y registrar el ingreso |
| ADMIN | `/admin/eventos`, `/admin/eventos/nuevo` | Crear borradores, buscarlos por código, asociar artistas y publicar eventos |

Para una demostración en una **base local de pruebas**, ejecuta [`tests/e2e/fixtures/seed-e2e.sql`](./tests/e2e/fixtures/seed-e2e.sql) después de que Flyway termine. Desde la raíz, si usas el contenedor `pulsepass-db` que se muestra más abajo:

```powershell
Get-Content -Raw -LiteralPath 'tests/e2e/fixtures/seed-e2e.sql' |
  docker exec -i pulsepass-db psql -v ON_ERROR_STOP=1 -U postgres -d pulsepass
```

El SQL agrega sedes y las cuentas de prueba `admin_e2e` y `staff_e2e`, con contraseñas desechables descritas en el propio archivo. Crea el usuario asistente desde `/registro`. Con el rol ADMIN, crea y publica un evento para que aparezca en la cartelera. Con USER, compra dos entradas. Puedes cancelar una desde Mis tickets y marcar la otra como usada con STAFF.

Desde `frontend/`:

```powershell
npm.cmd run lint
npm.cmd run build
npm.cmd test
```

El build verifica TypeScript estricto y genera `frontend/dist/`. Las pruebas usan Vitest, Testing Library y MSW y pueden ejecutarse sin el backend. Incluyen comprobaciones de accesibilidad del DOM con axe-core.

React Router carga las páginas por demanda. TanStack Query administra los datos de la API y actualiza las vistas después de cada operación. Zod valida las respuestas y se integra con React Hook Form en los formularios. La sesión dura lo que indique el JWT y se conserva en la pestaña. Spring Boot aplica la autorización y las reglas de compra.

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

Incluye registro, autenticación stateless con JWT y autorización por roles y propiedad del recurso.

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

### Autenticación

| Método | Ruta | Acceso | Respuesta correcta |
|---|---|---|---|
| `POST` | `/api/auth/login` | Público | `200 OK` con access token |
| `GET` | `/api/auth/me` | Autenticado | `200 OK` con identidad mínima |

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

### Política de acceso

- `GET` de eventos publicados, artistas y venues son públicos. Los eventos no publicados solo son visibles para `STAFF` y `ADMIN`.
- Crear/publicar eventos y asociar artistas requiere `ADMIN`.
- Comprar, consultar por correo y cancelar tickets exige que el recurso corresponda al principal, excepto consulta/cancelación administrativa autorizada. `STAFF` puede consultar un ticket por código y marcar tickets como usados, pero no comprar para terceros, listar tickets de terceros ni cancelarlos.
- Consultar ventas pagadas requiere `STAFF` o `ADMIN`.
- Las búsquedas de datos de usuario permiten al titular o a `ADMIN`.
- El registro público siempre crea `ROLE_USER`; no admite asignar roles elevados.
- Las rutas protegidas reciben el JWT solo mediante `Authorization: Bearer <token>`. El token de acceso dura 900 segundos por defecto; no hay refresh token ni revocación inmediata.
- Las respuestas HTTP incluyen `X-Request-ID`; login y denegaciones se registran con ese identificador, nunca con contraseñas, hashes o tokens.

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
	"password": "UnaFraseSeguraLarga2026!",
	"firstName": "Andrea",
	"lastName": "Lopez",
	"phone": "3001234567",
	"city": "Santa Marta",
	"birthDate": "2000-01-01"
}
```

Iniciar sesión:

```http
POST /api/auth/login
Content-Type: application/json

{"identifier":"andrea@example.com","password":"UnaFraseSeguraLarga2026!"}
```

La respuesta contiene `accessToken`, `tokenType` (`Bearer`) y `expiresIn`. La clave secreta no se versiona.
La contraseña se almacena en BCrypt dentro de la tabla `user_credentials`; las cuentas legadas sin credencial
no pueden iniciar sesión hasta que se les active una credencial por un proceso seguro.

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

## Configuración JWT

La API requiere una clave aleatoria Base64 de al menos 32 bytes. No guardes la clave en el repositorio.

En Windows PowerShell, establece una clave para la sesión actual:

```powershell
$jwtKey = [byte[]]::new(32)
[System.Security.Cryptography.RandomNumberGenerator]::Fill($jwtKey)
$env:PULSEPASS_JWT_SECRET_BASE64 = [Convert]::ToBase64String($jwtKey)
```

En macOS/Linux:

```bash
export PULSEPASS_JWT_SECRET_BASE64="$(openssl rand -base64 32)"
```

La configuración acepta también `PULSEPASS_JWT_ISSUER` y `PULSEPASS_JWT_ACCESS_TOKEN_TTL_SECONDS`; por defecto el emisor es `pulsepass-api` y el token dura 900 segundos. La aplicación falla al arrancar si falta la clave o no tiene el tamaño requerido.

Para permitir solicitudes CORS desde un frontend, configura `PULSEPASS_CORS_ALLOWED_ORIGINS` como una lista
separada por comas de orígenes exactos (por ejemplo, `https://app.example.com`). Por defecto no se autorizan
orígenes CORS y no se envían cookies.

En producción activa el perfil `prod` (`SPRING_PROFILES_ACTIVE=prod`) y configura `PULSEPASS_CORS_ALLOWED_ORIGINS`
según los orígenes aprobados. El perfil exige HTTPS y habilita la interpretación de cabeceras Forwarded; solo
debe usarse detrás de un proxy de confianza que elimine las cabeceras Forwarded entrantes y establezca las suyas.
El proxy debe bloquear el acceso directo externo a la aplicación. `localhost`, `127.0.0.1` y `::1` quedan
permitidos para facilitar el trabajo local. En entornos aislados de laboratorio, `PULSEPASS_REQUIRE_HTTPS=false`
permite mantener HTTP según el despliegue.

`X-Request-ID` puede suministrarse como UUID para correlación; si falta o no es válido, la API genera uno.
No se aceptan valores arbitrarios en los logs.

Los roles `STAFF` y `ADMIN` se asignan únicamente mediante un proceso administrativo controlado sobre `user_roles`
en entornos de desarrollo/demo. Por ejemplo, tras verificar el usuario destino y de forma explícita:

```sql
INSERT INTO user_roles (user_id, role)
SELECT id, 'ADMIN' FROM users WHERE username = 'admin-demo';
```

No ejecutar esa operación con valores recibidos por una solicitud pública ni reutilizar la cuenta demo en producción.

La autorización global de `STAFF` para consultar un ticket por código y para marcarlo como usado es una limitación
del MVP: el dominio todavía no relaciona personal operativo con eventos. En un despliegue real debe limitarse por
evento. También se recomienda añadir rate limiting y protección contra fuerza bruta al login, gestión/rotación de
claves con un gestor de secretos, y monitoreo centralizado de los eventos de seguridad. Esos controles operativos
no se incluyen en esta fase. Esta entrega tampoco establece un umbral ni un resultado de prueba de carga; mide
el rendimiento de autenticación con una instancia PostgreSQL representativa antes de producción.

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

### Verificación de seguridad y rendimiento

`SecurityEndpointMatrixTest` comprueba el acceso anónimo a las 20 operaciones heredadas y que USER/STAFF no ejecuten las operaciones reservadas a ADMIN; también verifica las operaciones de tickets permitidas a STAFF. Las pruebas de `AuthorizationService` cubren propietario frente a tercero para compras, consultas, cancelaciones y datos personales. `EventServiceImplTest` comprueba que los eventos no publicados no se expongan a usuarios públicos.

`SecurityMigrationIntegrationTest` aplica las migraciones hasta V4, inserta una cuenta legada y actualiza a la versión actual. Verifica que reciba solamente `USER`, no una contraseña inventada, y que repetir Flyway no altere el resultado.

La prueba de rendimiento usa PostgreSQL en Testcontainers, descarta tres logins de calentamiento y mide 20 logins secuenciales; imprime promedio, p50, p95, throughput y sentencias SQL/login. También falla si la carga del principal y sus roles supera dos sentencias SQL por login. Se puede ejecutar de forma aislada:

```powershell
.\mvnw.cmd "-Dtest=AuthenticationPerformanceIntegrationTest" test
```

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

- Repositorio: <https://github.com/nromero-4/pulsepass>
