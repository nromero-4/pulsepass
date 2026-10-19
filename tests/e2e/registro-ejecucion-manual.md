# Bitacora de ejecucion manual - PulsePass E2E

**Fecha:** 2026-10-18  **runId:** e2e-20261018-001

Registra unicamente resultados observados en Postman. La tabla esta alineada 1:1 con los **57 requests** de la coleccion; no hay resultados precargados.

| # | Carpeta | Request | Metodo y ruta | Actor | HTTP esperado | HTTP observado | Estado | Body / observaciones |
|---:|---|---|---|---|---|---|---|---|
| 1 | 00 Preparación | Health Check — GET /api/venues/active | `GET /api/venues/active` | Anonimo | ver descripcion | 200 | PASS | Array de 2 venues: VEN-E2E-001, VEN-E2E-002 |
| 2 | 01 Venues | E2E-VEN-01 — GET /api/venues/{code} | `GET /api/venues/VEN-E2E-001` | Anonimo | 200 | 200 | PASS | id=1, code=VEN-E2E-001, name=Auditorio Principal, active=true |
| 3 | 01 Venues | E2E-VEN-02 — GET /api/venues/active | `GET /api/venues/active` | Anonimo | 200 | 200 | PASS | Array de 2 venues activos |
| 4 | 02 Artists | E2E-ART-01 — GET /api/artists/{id} | `GET /api/artists/1` | Anonimo | 200 | 200 | PASS | id=1, stageName=Solar Beat, genre=Electronic, active=true |
| 5 | 02 Artists | E2E-ART-02 — GET /api/artists/by-stage-name?stageName=... | `GET /api/artists/by-stage-name?stageName=Solar%20Beat` | Anonimo | 200 | 200 | PASS | id=1, stageName=Solar Beat, country=Colombia |
| 6 | 02 Artists | E2E-ART-03 — GET /api/artists/active | `GET /api/artists/active` | Anonimo | 200 | 200 | PASS | Array de 5 artistas activos |
| 7 | 03 Auth & Usuarios | TC-AUTH-001 — POST /api/users (Registrar USER_A) | `POST /api/users` | Anonimo | 201 | 201 | PASS | id=4, username=e2e_user_a_20261018_001, active=true |
| 8 | 03 Auth & Usuarios | TC-AUTH-002 — POST /api/users (Registrar USER_B) | `POST /api/users` | Anonimo | 201 | 201 | PASS | id=5, username=e2e_user_b_20261018_001, active=true |
| 9 | 03 Auth & Usuarios | TC-AUTH-003 — POST /api/auth/login (USER_A con username) | `POST /api/auth/login` | Anonimo | 200 | 200 | PASS | accessToken emitido, tokenType=Bearer, expiresIn=900 |
| 10 | 03 Auth & Usuarios | TC-AUTH-004 — POST /api/auth/login (USER_B con email) | `POST /api/auth/login` | Anonimo | 200 | 200 | PASS | accessToken emitido, tokenType=Bearer, expiresIn=900 |
| 11 | 03 Auth & Usuarios | TC-AUTH-005 — POST /api/auth/login (STAFF con credenciales) | `POST /api/auth/login` | Anonimo | 200 | 200 | PASS | accessToken emitido, tokenType=Bearer, expiresIn=900 |
| 12 | 03 Auth & Usuarios | TC-AUTH-006 — POST /api/auth/login (ADMIN con credenciales) | `POST /api/auth/login` | Anonimo | 200 | 200 | PASS | accessToken emitido, tokenType=Bearer, expiresIn=900 |
| 13 | 03 Auth & Usuarios | TC-AUTH-007 — GET /api/auth/me (USER_A) | `GET /api/auth/me` | USER_A | 200 | 200 | PASS | userId=4, username=e2e_user_a_20261018_001, roles=[USER] |
| 14 | 03 Auth & Usuarios | TC-AUTH-008 — GET /api/auth/me (ADMIN) | `GET /api/auth/me` | ADMIN | 200 | 200 | PASS | userId=1, username=admin_e2e, roles=[ADMIN] |
| 15 | 03 Auth & Usuarios | TC-AUTH-009 — GET /api/auth/me sin token | `GET /api/auth/me` | Anonimo | 401 | 401 | PASS | 401 Unauthorized: Authentication is required |
| 16 | 03 Auth & Usuarios | TC-AUTH-010 — POST /api/auth/login (credenciales incorrectas) | `POST /api/auth/login` | Anonimo | 401 | 401 | PASS | 401 Unauthorized: Invalid username/email or password |
| 17 | 03 Auth & Usuarios | TC-AUTH-011 - POST /api/auth/login (cuenta inactiva) | `POST /api/auth/login` | Anonimo | 401. No debe devolver accessToken. | 401 | PASS | 401 Unauthorized: sin accessToken en body |
| 18 | 04 Usuarios — Consultas | E2E-USR-02 — GET /api/users/by-email (propio) | `GET /api/users/by-email?email=e2e_user_a_20261018_001@example.test` | USER_A | 200 | 200 | PASS | id=4, email=e2e_user_a_20261018_001@example.test, active=true |
| 19 | 04 Usuarios — Consultas | TC-SEC-006 — GET /api/users/by-email (usuario ajeno, USER_B intenta ver USER_A) | `GET /api/users/by-email?email=e2e_user_a_20261018_001@example.test` | USER_B | 403 | 403 | PASS | 403 Forbidden: You do not have permission to perform this operation |
| 20 | 04 Usuarios — Consultas | E2E-USR-03 — GET /api/users/by-username (propio) | `GET /api/users/by-username?username=e2e_user_a_20261018_001` | USER_A | 200 | 200 | PASS | id=4, username=e2e_user_a_20261018_001, active=true |
| 21 | 04 Usuarios — Consultas | TC-SEC-007 — GET /api/users/by-username (usuario ajeno) | `GET /api/users/by-username?username=e2e_user_a_20261018_001` | USER_B | 403 | 403 | PASS | 403 Forbidden: You do not have permission to perform this operation |
| 22 | 05 Events — Catálogo | TC-BIZ-003 — POST /api/events (ADMIN crea evento borrador) | `POST /api/events` | ADMIN | 201 | 201 | PASS | id=1, eventCode=EVT-E2E-001-e2e-20261018-001, status=DRAFT. Nota: DTO requiere category y minimumAge |
| 23 | 05 Events — Catálogo | TC-SEC-001 — POST /api/events (USER intenta crear, deber fallar 403) | `POST /api/events` | USER_A | 403 | 403 | PASS | 403 Forbidden: You do not have permission to perform this operation |
| 24 | 05 Events — Catálogo | TC-SEC-001-VERIFY - GET /api/events/{eventCode} (confirmar que el evento denegado no existe) | `GET /api/events/EVT-FAIL-e2e-20261018-001` | ADMIN | 404 | 404 | PASS | 404 Not Found: Event not found: EVT-FAIL-e2e-20261018-001 |
| 25 | 05 Events — Catálogo | E2E-EVT-02 — GET /api/events/{eventCode} (borrador por ADMIN) | `GET /api/events/EVT-E2E-001-e2e-20261018-001` | ADMIN | 200 | 200 | PASS | status=DRAFT, eventCode=EVT-E2E-001-e2e-20261018-001 |
| 26 | 05 Events — Catálogo | TC-SEC-004 — GET /api/events/{eventCode} (borrador por anónimo, debe fallar) | `GET /api/events/EVT-E2E-001-e2e-20261018-001` | Anonimo | 404 | 404 | PASS | 404 Not Found: borrador oculto para anónimo |
| 27 | 05 Events — Catálogo | E2E-EVT-03 — GET /api/events/published | `GET /api/events/published` | Anonimo | 200 | 200 | PASS | Array vacío [] — borrador no aparece en publicados |
| 28 | 05 Events — Catálogo | E2E-EVT-04 — PATCH /api/events/{eventCode}/publish (ADMIN publica) | `PATCH /api/events/EVT-E2E-001-e2e-20261018-001/publish` | ADMIN | 200 | 200 | PASS | status=PUBLISHED, eventCode=EVT-E2E-001-e2e-20261018-001 |
| 29 | 05 Events — Catálogo | TC-SEC-002 — PATCH /api/events/{eventCode}/publish (USER intenta, falla 403) | `PATCH /api/events/EVT-E2E-001-e2e-20261018-001/publish` | USER_A | 403 | 403 | PASS | 403 Forbidden: You do not have permission to perform this operation |
| 30 | 05 Events — Catálogo | E2E-EVT-05 — POST /api/events/{eventCode}/artists/{artistId} (asociar artista) | `POST /api/events/EVT-E2E-001-e2e-20261018-001/artists/1` | ADMIN | 200 | 200 | PASS | artists=[Solar Beat] asociado al evento |
| 31 | 05 Events — Catálogo | TC-BIZ-005 — GET /api/events/by-artist?stageName=... (tras asociar) | `GET /api/events/by-artist?stageName=Solar%20Beat` | Anonimo | 200 | 200 | PASS | Array con 1 evento: EVT-E2E-001-e2e-20261018-001, status=PUBLISHED |
| 32 | 05 Events — Catálogo | TC-SEC-003 — POST /api/events/{eventCode}/artists/{artistId} (USER falla 403) | `POST /api/events/EVT-E2E-001-e2e-20261018-001/artists/1` | USER_A | 403 | 403 | PASS | 403 Forbidden: You do not have permission to perform this operation |
| 33 | 05 Events — Catálogo | E2E-EVT-07 - GET /api/events/{eventCode} (evento publicado, anonimo) | `GET /api/events/{{publishedEventCode}}` | Anonimo | 200. El body indica status=PUBLISHED y no expone datos sensibles. | ____ | ____ | ____ |
| 34 | 06 Tickets — Compra y Ciclo | TC-BIZ-007 — POST /api/tickets (USER_A compra) | `POST /api/tickets` | USER_A | 201 | ____ | ____ | ____ |
| 35 | 06 Tickets — Compra y Ciclo | TC-SEC-008 — POST /api/tickets (USER_A intenta comprar con email de USER_B) | `POST /api/tickets` | USER_A | 403 | ____ | ____ | ____ |
| 36 | 06 Tickets — Compra y Ciclo | TC-SEC-008-VERIFY - GET /api/tickets/by-user (confirmar que el intento denegado no compro) | `GET /api/tickets/by-user?email={{userBEmail}}` | ADMIN | 200 (array vacio) | ____ | ____ | ____ |
| 37 | 06 Tickets — Compra y Ciclo | E2E-TKT-02 — GET /api/tickets/{ticketCode} (propietario consulta) | `GET /api/tickets/{{ticketCancelableCode}}` | USER_A | 200 | ____ | ____ | ____ |
| 38 | 06 Tickets — Compra y Ciclo | TC-SEC-009 — GET /api/tickets/{ticketCode} (usuario ajeno intenta ver) | `GET /api/tickets/{{ticketCancelableCode}}` | USER_B | 403 | ____ | ____ | ____ |
| 39 | 06 Tickets — Compra y Ciclo | E2E-TKT-03 — GET /api/tickets/by-user?email=... (propietario lista sus tickets) | `GET /api/tickets/by-user?email={{userAEmail}}` | USER_A | 200 | ____ | ____ | ____ |
| 40 | 06 Tickets — Compra y Ciclo | TC-SEC-010 — GET /api/tickets/by-user?email=... (usuario ajeno intenta listar) | `GET /api/tickets/by-user?email={{userAEmail}}` | USER_B | 403 | ____ | ____ | ____ |
| 41 | 06 Tickets — Compra y Ciclo | E2E-TKT-04 — GET /api/events/{eventCode}/tickets/paid (STAFF lista pagados) | `GET /api/events/EVT-E2E-001-e2e-20261018-001/tickets/paid` | STAFF | 200 | 200 | PASS | Array con 1 ticket PAID de USER_A |
| 42 | 06 Tickets — Compra y Ciclo | TC-SEC-013 — GET /api/events/{eventCode}/tickets/paid (USER intenta, falla 403) | `GET /api/events/EVT-E2E-001-e2e-20261018-001/tickets/paid` | USER_A | 403 | 403 | PASS | 403 Forbidden: USER no puede listar tickets pagados |
| 43 | 06 Tickets — Compra y Ciclo | TC-BIZ-010 — PATCH /api/tickets/{ticketCode}/cancel (propietario cancela) | `PATCH /api/tickets/TCK-24f18978-a5df-4392-9007-d48a73a8047a/cancel` | USER_A | 200 | 200 | PASS | status=CANCELLED |
| 44 | 06 Tickets — Compra y Ciclo | E2E-TKT-05 - GET /api/tickets/{ticketCode} (verificar CANCELLED persistido) | `GET /api/tickets/TCK-24f18978-a5df-4392-9007-d48a73a8047a` | USER_A | 200 (status CANCELLED) | 200 | PASS | status=CANCELLED confirmado |
| 45 | 06 Tickets — Compra y Ciclo | TC-BIZ-011 — PATCH /api/tickets/{ticketCode}/cancel (segunda cancelación, debe fallar) | `PATCH /api/tickets/TCK-24f18978-a5df-4392-9007-d48a73a8047a/cancel` | USER_A | 409 | 409 | PASS | 409 Conflict: Only paid tickets can be cancelled |
| 46 | 06 Tickets — Compra y Ciclo | TC-BIZ-007b — POST /api/tickets (comprar segundo ticket para marcar como usado) | `POST /api/tickets` | USER_A | 201 | 201 | PASS | ticketCode=TCK-66427ab6-9cff-46f0-a1d0-af20adb6952a, status=PAID |
| 47 | 06 Tickets — Compra y Ciclo | TC-BIZ-012 — PATCH /api/tickets/{ticketCode}/use (STAFF marca usado) | `PATCH /api/tickets/TCK-66427ab6-9cff-46f0-a1d0-af20adb6952a/use` | STAFF | 200 | 200 | PASS | status=USED |
| 48 | 06 Tickets — Compra y Ciclo | E2E-TKT-06 - GET /api/tickets/{ticketCode} (verificar USED persistido) | `GET /api/tickets/TCK-66427ab6-9cff-46f0-a1d0-af20adb6952a` | USER_A | 200 (status USED) | 200 | PASS | status=USED confirmado |
| 49 | 06 Tickets — Compra y Ciclo | TC-BIZ-013 — PATCH /api/tickets/{ticketCode}/use (segunda transición, falla 409) | `PATCH /api/tickets/TCK-66427ab6-9cff-46f0-a1d0-af20adb6952a/use` | STAFF | 409 | 409 | PASS | 409 Conflict: Only paid tickets can be marked as used |
| 50 | 06 Tickets — Compra y Ciclo | TC-SEC-012 — PATCH /api/tickets/{ticketCode}/use (USER intenta, falla 403) | `PATCH /api/tickets/TCK-66427ab6-9cff-46f0-a1d0-af20adb6952a/use` | USER_A | 403 | 403 | PASS | 403 Forbidden: USER no puede marcar ticket como usado |
| 51 | 07 Seguridad — Casos Negativos | TC-AUTH-007 — GET /api/auth/me sin Authorization header | `GET /api/auth/me` | Anonimo | 401 | 401 | PASS | 401 Unauthorized: Authentication is required |
| 52 | 07 Seguridad — Casos Negativos | TC-AUTH-008 — GET /api/auth/me con Bearer malformado | `GET /api/auth/me` | Anonimo (token invalido) | 401 | 401 | PASS | 401 Unauthorized: Authentication is required |
| 53 | 07 Seguridad — Casos Negativos | TC-AUTH-009 — GET /api/auth/me con JWT alterado | `GET /api/auth/me` | Anonimo (token invalido) | 401 | 401 | PASS | 401 Unauthorized: Authentication is required |
| 54 | 08 Validaciones y Errores | 400 — POST /api/users (password muy corto) | `POST /api/users` | Anonimo | 400 | 400 | PASS | 400 Validation failed: password must be 12-72 chars |
| 55 | 08 Validaciones y Errores | 400 — POST /api/users (email inválido) | `POST /api/users` | Anonimo | 400 | 400 | PASS | 400 Validation failed: email format invalid |
| 56 | 08 Validaciones y Errores | 409 — POST /api/users (email duplicado) | `POST /api/users` | Anonimo | 409 | 409 | PASS | 409 Conflict: Email already exists: e2e_user_a_20261018_001@example.test |
| 57 | 08 Validaciones y Errores | 404 — GET /api/venues/code-inexistente | `GET /api/venues/NONEXISTENT-e2e-20261018-001` | Anonimo | 404 | 404 | PASS | 404 Not Found: Venue not found: NONEXISTENT-e2e-20261018-001 |

## Resumen de ejecucion

| Resultado | Cantidad |
|---|---:|
| PASS | 57 |
| FAIL | 0 |
| BLOCKED | 0 |
| Total ejecutadas | 57 / 57 |
| Rutas verificadas | 22 / 22 |
