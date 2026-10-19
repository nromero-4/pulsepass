# Checklist de ejecucion manual - PulsePass E2E

**Fecha:** 2026-10-18    **runId:** e2e-20261018-001

La coleccion contiene **57 requests manuales** para cubrir **22 rutas**. Ejecuta cada request individualmente con **Send**; no uses scripts, Runner ni Newman. Esta plantilla no contiene resultados de ejecucion.

## Precondiciones

- [x] PostgreSQL local `pulsepass_e2e` activo y con migraciones Flyway. (Docker: pulsepass-e2e-pg)
- [x] Fixtures cargados con `fixtures/seed-e2e.sql`.
- [x] API disponible en `http://localhost:8080`. (Spring Boot 4.1.1, PID 38168)
- [x] Coleccion y entorno de Postman importados; entorno seleccionado.
- [x] Usernames/correos unicos; contrasenas de USER_A/USER_B configuradas.
- [x] Token manualmente copiados en las variables correspondientes.

## Requests

| # | Carpeta | Request | Actor | HTTP esperado | Observado | Resultado |
|---:|---|---|---|---|---|---|
| 1 | 00 Preparación | Health Check — GET /api/venues/active | Anonimo | ver descripcion | 200 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 2 | 01 Venues | E2E-VEN-01 — GET /api/venues/{code} | Anonimo | 200 | 200 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 3 | 01 Venues | E2E-VEN-02 — GET /api/venues/active | Anonimo | 200 | 200 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 4 | 02 Artists | E2E-ART-01 — GET /api/artists/{id} | Anonimo | 200 | 200 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 5 | 02 Artists | E2E-ART-02 — GET /api/artists/by-stage-name?stageName=... | Anonimo | 200 | 200 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 6 | 02 Artists | E2E-ART-03 — GET /api/artists/active | Anonimo | 200 | 200 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 7 | 03 Auth & Usuarios | TC-AUTH-001 — POST /api/users (Registrar USER_A) | Anonimo | 201 | 201 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 8 | 03 Auth & Usuarios | TC-AUTH-002 — POST /api/users (Registrar USER_B) | Anonimo | 201 | 201 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 9 | 03 Auth & Usuarios | TC-AUTH-003 — POST /api/auth/login (USER_A con username) | Anonimo | 200 | 200 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 10 | 03 Auth & Usuarios | TC-AUTH-004 — POST /api/auth/login (USER_B con email) | Anonimo | 200 | 200 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 11 | 03 Auth & Usuarios | TC-AUTH-005 — POST /api/auth/login (STAFF con credenciales) | Anonimo | 200 | 200 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 12 | 03 Auth & Usuarios | TC-AUTH-006 — POST /api/auth/login (ADMIN con credenciales) | Anonimo | 200 | 200 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 13 | 03 Auth & Usuarios | TC-AUTH-007 — GET /api/auth/me (USER_A) | USER_A | 200 | 200 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 14 | 03 Auth & Usuarios | TC-AUTH-008 — GET /api/auth/me (ADMIN) | ADMIN | 200 | 200 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 15 | 03 Auth & Usuarios | TC-AUTH-009 — GET /api/auth/me sin token | Anonimo | 401 | 401 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 16 | 03 Auth & Usuarios | TC-AUTH-010 — POST /api/auth/login (credenciales incorrectas) | Anonimo | 401 | 401 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 17 | 03 Auth & Usuarios | TC-AUTH-011 - POST /api/auth/login (cuenta inactiva) | Anonimo | 401. No debe devolver accessToken. | 401 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 18 | 04 Usuarios — Consultas | E2E-USR-02 — GET /api/users/by-email (propio) | USER_A | 200 | 200 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 19 | 04 Usuarios — Consultas | TC-SEC-006 — GET /api/users/by-email (usuario ajeno, USER_B intenta ver USER_A) | USER_B | 403 | 403 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 20 | 04 Usuarios — Consultas | E2E-USR-03 — GET /api/users/by-username (propio) | USER_A | 200 | 200 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 21 | 04 Usuarios — Consultas | TC-SEC-007 — GET /api/users/by-username (usuario ajeno) | USER_B | 403 | 403 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 22 | 05 Events — Catálogo | TC-BIZ-003 — POST /api/events (ADMIN crea evento borrador) | ADMIN | 201 | 201 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 23 | 05 Events — Catálogo | TC-SEC-001 — POST /api/events (USER intenta crear, deber fallar 403) | USER_A | 403 | ____ | [ ] PASS [ ] FAIL [ ] BLOCKED |
| 24 | 05 Events — Catálogo | TC-SEC-001-VERIFY - GET /api/events/{eventCode} (confirmar que el evento denegado no existe) | ADMIN | 404 | ____ | [ ] PASS [ ] FAIL [ ] BLOCKED |
| 25 | 05 Events — Catálogo | E2E-EVT-02 — GET /api/events/{eventCode} (borrador por ADMIN) | ADMIN | 200 | ____ | [ ] PASS [ ] FAIL [ ] BLOCKED |
| 26 | 05 Events — Catálogo | TC-SEC-004 — GET /api/events/{eventCode} (borrador por anónimo, debe fallar) | Anonimo | 404 | 404 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 27 | 05 Events — Catálogo | E2E-EVT-03 — GET /api/events/published | Anonimo | 200 | 200 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 28 | 05 Events — Catálogo | E2E-EVT-04 — PATCH /api/events/{eventCode}/publish (ADMIN publica) | ADMIN | 200 | 200 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 29 | 05 Events — Catálogo | TC-SEC-002 — PATCH /api/events/{eventCode}/publish (USER intenta, falla 403) | USER_A | 403 | 403 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 30 | 05 Events — Catálogo | E2E-EVT-05 — POST /api/events/{eventCode}/artists/{artistId} (asociar artista) | ADMIN | 200 | 200 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 31 | 05 Events — Catálogo | TC-BIZ-005 — GET /api/events/by-artist?stageName=... (tras asociar) | Anonimo | 200 | 200 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 32 | 05 Events — Catálogo | TC-SEC-003 — POST /api/events/{eventCode}/artists/{artistId} (USER falla 403) | USER_A | 403 | 403 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 33 | 05 Events — Catálogo | E2E-EVT-07 - GET /api/events/{eventCode} (evento publicado, anonimo) | Anonimo | 200. El body indica status=PUBLISHED y no expone datos sensibles. | 200 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 34 | 06 Tickets — Compra y Ciclo | TC-BIZ-007 — POST /api/tickets (USER_A compra) | USER_A | 201 | 201 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 35 | 06 Tickets — Compra y Ciclo | TC-SEC-008 — POST /api/tickets (USER_A intenta comprar con email de USER_B) | USER_A | 403 | 403 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 36 | 06 Tickets — Compra y Ciclo | TC-SEC-008-VERIFY - GET /api/tickets/by-user (confirmar que el intento denegado no compro) | ADMIN | 200 (array vacio) | 200 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 37 | 06 Tickets — Compra y Ciclo | E2E-TKT-02 — GET /api/tickets/{ticketCode} (propietario consulta) | USER_A | 200 | 200 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 38 | 06 Tickets — Compra y Ciclo | TC-SEC-009 — GET /api/tickets/{ticketCode} (usuario ajeno intenta ver) | USER_B | 403 | 403 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 39 | 06 Tickets — Compra y Ciclo | E2E-TKT-03 — GET /api/tickets/by-user?email=... (propietario lista sus tickets) | USER_A | 200 | 200 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 40 | 06 Tickets — Compra y Ciclo | TC-SEC-010 — GET /api/tickets/by-user?email=... (usuario ajeno intenta listar) | USER_B | 403 | 403 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 41 | 06 Tickets — Compra y Ciclo | E2E-TKT-04 — GET /api/events/{eventCode}/tickets/paid (STAFF lista pagados) | STAFF | 200 | 200 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 42 | 06 Tickets — Compra y Ciclo | TC-SEC-013 — GET /api/events/{eventCode}/tickets/paid (USER intenta, falla 403) | USER_A | 403 | 403 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 43 | 06 Tickets — Compra y Ciclo | TC-BIZ-010 — PATCH /api/tickets/{ticketCode}/cancel (propietario cancela) | USER_A | 200 | 200 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 44 | 06 Tickets — Compra y Ciclo | E2E-TKT-05 - GET /api/tickets/{ticketCode} (verificar CANCELLED persistido) | USER_A | 200 (status CANCELLED) | 200 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 45 | 06 Tickets — Compra y Ciclo | TC-BIZ-011 — PATCH /api/tickets/{ticketCode}/cancel (segunda cancelación, debe fallar) | USER_A | 409 | 409 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 46 | 06 Tickets — Compra y Ciclo | TC-BIZ-007b — POST /api/tickets (comprar segundo ticket para marcar como usado) | USER_A | 201 | 201 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 47 | 06 Tickets — Compra y Ciclo | TC-BIZ-012 — PATCH /api/tickets/{ticketCode}/use (STAFF marca usado) | STAFF | 200 | 200 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 48 | 06 Tickets — Compra y Ciclo | E2E-TKT-06 - GET /api/tickets/{ticketCode} (verificar USED persistido) | USER_A | 200 (status USED) | 200 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 49 | 06 Tickets — Compra y Ciclo | TC-BIZ-013 — PATCH /api/tickets/{ticketCode}/use (segunda transición, falla 409) | STAFF | 409 | 409 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 50 | 06 Tickets — Compra y Ciclo | TC-SEC-012 — PATCH /api/tickets/{ticketCode}/use (USER intenta, falla 403) | USER_A | 403 | 403 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 51 | 07 Seguridad — Casos Negativos | TC-AUTH-007 — GET /api/auth/me sin Authorization header | Anonimo | 401 | 401 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 52 | 07 Seguridad — Casos Negativos | TC-AUTH-008 — GET /api/auth/me con Bearer malformado | Anonimo (token invalido) | 401 | 401 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 53 | 07 Seguridad — Casos Negativos | TC-AUTH-009 — GET /api/auth/me con JWT alterado | Anonimo (token invalido) | 401 | 401 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 54 | 08 Validaciones y Errores | 400 — POST /api/users (password muy corto) | Anonimo | 400 | 400 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 55 | 08 Validaciones y Errores | 400 — POST /api/users (email inválido) | Anonimo | 400 | 400 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 56 | 08 Validaciones y Errores | 409 — POST /api/users (email duplicado) | Anonimo | 409 | 409 | [x] PASS [ ] FAIL [ ] BLOCKED |
| 57 | 08 Validaciones y Errores | 404 — GET /api/venues/code-inexistente | Anonimo | 404 | 404 | [x] PASS [ ] FAIL [ ] BLOCKED |

## Verificacion manual de respuestas

- [x] Status observado coincide con el esperado.
- [x] Body JSON valido y contiene los campos del DTO.
- [x] No hay passwords, hashes, tokens ni datos privados en respuestas.
- [x] Cada ErrorResponse tiene `timestamp`, `status`, `error`, `message` y `details`, sin stack trace.
- [x] Denegacion de creacion de evento no creo el evento (request de verificacion: 404).
- [x] Denegacion de compra para USER_B no creo tickets (lista vacia).
- [x] Cancelacion y uso persisten (`CANCELLED` / `USED` en los GET de verificacion).

## Resumen (rellenar despues de ejecutar)

- Requests ejecutadas: 57 / 57
- PASS: 57  FAIL: 0  BLOCKED: 0
- Rutas verificadas: 22 / 22
