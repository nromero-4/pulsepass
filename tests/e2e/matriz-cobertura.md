# Matriz de Cobertura — PulsePass E2E Manual

**Versión:** 1.2
**Fecha:** 2026-10-18  
**Objetivo:** Cobertura de 22 rutas + autorización + propiedad + casos negativos  

---

## Leyenda

- **✓ Public** = Acceso sin autenticación
- **A** = Requiere autenticación (Bearer JWT)
- **ADMIN** = Requiere rol ADMIN
- **STAFF** = Requiere rol STAFF o ADMIN
- **OWN** = Propietario o ADMIN
- **IDOR\*** = Caso de acceso horizontal prohibido

---

## Matriz Completa: 22 Rutas × Roles × Casos

| # | Ruta | Método | Base | Público | 401 | USER | STAFF | ADMIN | IDOR\* | Casos Negativos |
|----|------|--------|------|---------|-----|------|-------|-------|--------|-----------------|
| **1** | `/api/venues/{code}` | GET | ✓ Public | 200 | N/A | 200 | 200 | 200 | N/A | 404 inexistente |
| **2** | `/api/venues/active` | GET | ✓ Public | 200 | N/A | 200 | 200 | 200 | N/A | Vacío OK |
| **3** | `/api/artists/{id}` | GET | ✓ Public | 200 | N/A | 200 | 200 | 200 | N/A | 404 inexistente |
| **4** | `/api/artists/by-stage-name` | GET | ✓ Public | 200 | N/A | 200 | 200 | 200 | N/A | 404 no coincide |
| **5** | `/api/artists/active` | GET | ✓ Public | 200 | N/A | 200 | 200 | 200 | N/A | Solo activos |
| **6** | `/api/auth/login` | POST | ✓ Public | 200 | N/A | 200 | 200 | 200 | N/A | 401 pwd incorrecto |
| **7** | `/api/auth/me` | GET | A | 401 | 401 | 200 | 200 | 200 | N/A | Token malformado |
| **8** | `/api/users` | POST | ✓ Public | 201 | N/A | 201 | 201 | 201 | N/A | 400 validación, 409 dup |
| **9** | `/api/users/by-email` | GET | A + OWN | 401 | 401 | 200/403 | 403 | 200 | 403 | Email privado |
| **10** | `/api/users/by-username` | GET | A + OWN | 401 | 401 | 200/403 | 403 | 200 | 403 | Username privado |
| **11** | `/api/events` | POST | ADMIN | 401 | 401 | 403 | 403 | 201 | N/A | 400 fecha pasada |
| **12** | `/api/events/{code}` | GET | Public/OPS | 200 (PUB) | 401 | 200 (PUB), 404 (DRAFT) | 200 (OPS) | 200 | 404 (DRAFT) | Draft oculto |
| **13** | `/api/events/published` | GET | ✓ Public | 200 | N/A | 200 | 200 | 200 | N/A | Solo PUBLISHED |
| **14** | `/api/events/{code}/publish` | PATCH | ADMIN | 401 | 401 | 403 | 403 | 200 | N/A | 409 no DRAFT |
| **15** | `/api/events/{code}/artists/{id}` | POST | ADMIN | 401 | 401 | 403 | 403 | 200 | N/A | 409 duplicado |
| **16** | `/api/events/by-artist` | GET | ✓ Public | 200 | N/A | 200 | 200 | 200 | N/A | Solo PUBLISHED |
| **17** | `/api/tickets` | POST | A (OWN) | 401 | 401 | 201 (own) | 201 | 201 | 403 (alien) | 409 no PUBLISHED |
| **18** | `/api/tickets/{code}` | GET | A (STAFF+) | 401 | 401 | 200 (own) | 200 | 200 | 403 | Propiedad |
| **19** | `/api/tickets/by-user` | GET | A (OWN/ADMIN) | 401 | 401 | 200 (own), 403 (other) | 403 (other) | 200 | 403 | Email ajeno |
| **20** | `/api/events/{code}/tickets/paid` | GET | STAFF | 401 | 401 | 403 | 200 | 200 | N/A | Solo PAID |
| **21** | `/api/tickets/{code}/cancel` | PATCH | A (OWN/ADMIN) | 401 | 401 | 200 (own) | 403 (other) | 200 | 403 (alien) | 409 no PAID |
| **22** | `/api/tickets/{code}/use` | PATCH | STAFF | 401 | 401 | 403 | 200 | 200 | N/A | 409 no PAID |

---

## Detalles de Cada Ruta

### 1. GET /api/venues/{code}
**Status esperados por actor:**
- Anónimo: **200** VenueResponse
- USER_A: **200** VenueResponse
- STAFF: **200** VenueResponse
- ADMIN: **200** VenueResponse

**Casos negativos:**
- `GET /api/venues/NONEXISTENT` → **404** ErrorResponse
- Venue inactivo: **200** pero `active: false`

**Validación de respuesta:**
- DTO: `{id, code, name, city, address, capacity, active}`
- Sin campos sensibles

---

### 2. GET /api/venues/active
**Status esperados:**
- Anónimo: **200** `List<VenueResponse>` (solo `active: true`)

**Validación:**
- Todos los venues tienen `active: true`
- Array no vacío (fixtures garantizan ≥2)

---

### 3-5. Artists
**GET /api/artists/{id}**, **GET /api/artists/by-stage-name**, **GET /api/artists/active**

**Status esperados:** **200** para todos (público)

**Validación:**
- DTO: `{id, stageName, country, genre, active}`
- `GET /api/artists/active` solo retorna `active: true`

---

### 6. POST /api/auth/login
**Status esperados:**
- Credenciales correctas: **200** AuthResponse
- Credenciales incorrectas: **401** genérico (sin enumeración)
- Identificador inexistente: **401** (igual que password incorrecto)

**Respuesta esperada:**
```json
{
  "accessToken": "<JWT_válido>",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

**Validación:**
- JWT decodeable (header.payload.signature)
- Claims: `sub` (user ID), `iss` (pulsepass-api), `exp`, `iat`, `jti`
- Token válido para usar en Bearer header
- No expira hasta 900 segundos

---

### 7. GET /api/auth/me
**Status esperados:**
- Sin header: **401** ErrorResponse
- Con token válido: **200** AuthenticatedUserResponse
- Con token expirado/malformado: **401** ErrorResponse
- Con token alterado: **401** ErrorResponse

**Respuesta esperada:**
```json
{
  "userId": <long>,
  "username": "<string>",
  "email": "<string>",
  "roles": ["USER", "STAFF", "ADMIN"]  // según rol
}
```

**Validación:**
- Sin password en respuesta
- Roles correctos (verificar contra asignación)
- Identidad coincide con token

---

### 8. POST /api/users
**Status esperados:**
- Registro válido: **201** UserResponse
- Email duplicado: **409** ErrorResponse
- Username duplicado: **409** ErrorResponse
- Email inválido: **400** ErrorResponse
- Password < 12 caracteres: **400** ErrorResponse
- Password > 72 caracteres UTF-8: **400** ErrorResponse

**Respuesta esperada:**
```json
{
  "id": <long>,
  "username": "<string>",
  "email": "<string>",
  "firstName": "<string>",
  "lastName": "<string>",
  "phone": "<string>",
  "city": "<string>",
  "birthDate": "<date>",
  "active": true
}
```

**Validación:**
- Sin `password`, `passwordHash` en respuesta
- Rol asignado: USER (no ADMIN, no STAFF)
- Todos los campos rellenados

---

### 9. GET /api/users/by-email
**Status esperados:**
- Usuario consultando su propio email: **200** UserResponse
- USER_A consultando email de USER_B: **403** (`AccessDenied`)
- ADMIN consultando cualquier email: **200** UserResponse
- Sin token: **401** ErrorResponse

**Validación de IDOR:**
- USER_B NO recibe datos de USER_A
- No se devuelve información del otro usuario; el rechazo es **403**

---

### 10. GET /api/users/by-username
**Status esperados:** Mismo que by-email (propiedad verificada)

---

### 11. POST /api/events
**Status esperados:**
- ADMIN crea evento: **201** EventResponse (estado DRAFT)
- USER crea evento: **403** ErrorResponse
- STAFF crea evento: **403** ErrorResponse
- Sin token: **401** ErrorResponse

**Validación de negocio:**
- Evento creado con status DRAFT
- Fecha futura requerida; fecha pasada → **409** BusinessRuleException
- Venue debe estar activo; venue inactivo → **409**
- EventCode único; duplicado → **409**

---

### 12. GET /api/events/{code}
**Status esperados:**
- Evento PUBLISHED, sin token: **200** EventResponse
- Evento DRAFT, sin token: **404** ErrorResponse
- Evento DRAFT, con ADMIN/STAFF token: **200** EventResponse
- Evento DRAFT, con USER token: **404**

**Validación:**
- Borradores nunca expuestos a público/usuarios regulares
- Status DRAFT no debe aparecer en GET /api/events/published

---

### 13-16. Events (consultas y operaciones)
**GET /api/events/published**, **PATCH /api/events/{code}/publish**, **POST /api/events/{code}/artists/{id}**, **GET /api/events/by-artist**

**Cobertura:**
- Crear evento DRAFT → listar publicados (no aparece)
- Publicar evento (ADMIN) → aparece en listados
- Asociar artista (ADMIN) → aparece en GET by-artist
- USER intenta crear/publicar/asociar → 403 (o 401 si sin token)

---

### 17. POST /api/tickets
**Status esperados:**
- USER compra para sí mismo: **201** TicketResponse (status PAID)
- USER intenta comprar para otro: **403** ErrorResponse
- USER compra para evento no PUBLISHED: **409** BusinessRuleException
- USER compra para evento pasado: **409** BusinessRuleException
- Capacidad agotada: **409** BusinessRuleException
- Sin token: **401** ErrorResponse

**Validación de negocio:**
- TicketStatus inicial: PAID (no RESERVED)
- Ticket vinculado a usuario correcto
- Evento status se actualiza a SOLD_OUT si se agota capacidad
- Comprador debe ser el userEmail (no aceptar email ajeno)

---

### 18-19. Tickets (consultas)
**GET /api/tickets/{code}**, **GET /api/tickets/by-user**

**Status esperados:**
- Propietario: **200** TicketResponse
- Otro usuario: **403** ErrorResponse (`AccessDenied`)
- STAFF: **200**
- ADMIN: **200**
- Sin token: **401**

**Validación de IDOR:**
- USER_A no ve tickets de USER_B
- Después de denegación, verificar con GET que recurso no cambió

---

### 20. GET /api/events/{code}/tickets/paid
**Status esperados:**
- STAFF: **200** List<TicketResponse> (solo status PAID)
- ADMIN: **200**
- USER: **403** ErrorResponse
- Sin token: **401**

**Validación:**
- Array contiene solo tickets con status PAID
- Ningún CANCELLED, USED, RESERVED

---

### 21. PATCH /api/tickets/{code}/cancel
**Status esperados:**
- Propietario (ticket PAID, evento futuro): **200** TicketResponse (status CANCELLED)
- Intento 2ª cancelación (ticket ya CANCELLED): **409** BusinessRuleException
- Ticket USED: **409** (no se puede cancelar)
- Evento pasado: **409** (no se puede cancelar)
- Otro usuario intenta: **403** ErrorResponse (`AccessDenied`)

**Validación:**
- Verificar con GET posterior que status cambió a CANCELLED
- No se ejecuta la segunda cancelación (re-leer muestra CANCELLED)

---

### 22. PATCH /api/tickets/{code}/use
**Status esperados:**
- STAFF marca ticket PAID: **200** TicketResponse (status USED)
- Intento 2ª marca (ticket ya USED): **409**
- Ticket CANCELLED: **409** (no se puede usar)
- USER intenta: **403**
- Sin token: **401**

**Validación:**
- Verificar con GET posterior que status cambió a USED

---

## Casos Negativos Obligatorios

### HTTP 400 (Bad Request)
- [ ] Email inválido (no tiene @)
- [ ] Password < 12 caracteres
- [ ] Cuerpo JSON malformado
- [ ] Campo obligatorio ausente

### HTTP 401 (Unauthorized)
- [ ] Sin header Authorization
- [ ] Token malformado (no es JWT)
- [ ] Token alterado (caractere cambiado)
- [ ] Credenciales incorrectas en login

### HTTP 403 (Forbidden)
- [ ] USER intenta crear evento (solo ADMIN)
- [ ] USER intenta listar tickets pagados (solo STAFF/ADMIN)
- [ ] Usuario ajeno intenta acceder a recurso (según política)
- [ ] ADMIN accesible desde USER (autorización)

### HTTP 404 (Not Found)
- [ ] Venue inexistente
- [ ] Artist inexistente
- [ ] Evento borrador consultado sin autorización
- [ ] Usuario ajeno (según política de IDOR)

### HTTP 409 (Conflict)
- [ ] Email duplicado al registrar
- [ ] Evento ya publicado (publicar DRAFT de nuevo)
- [ ] Ticket no PAID al cancelar
- [ ] Capacidad agotada al comprar

---

## ErrorResponse Validation

Todos los errores 4xx deben contener:

```json
{
  "timestamp": "2026-10-18T15:06:00",     // ISO 8601, presente
  "status": 403,                            // Coincide con HTTP code
  "error": "Forbidden",                     // Descripción del estado HTTP
  "message": "A user may only access...",   // Seguro, sin stack trace
  "details": {}                             // Vacío o detalles de validación
}
```

**Validaciones:**
- [ ] `timestamp` presente y válido (no null)
- [ ] `status` coincide exactamente con HTTP code
- [ ] `error` es el nombre del estado (no contiene datos)
- [ ] `message` es legible, sin internals, sin líneas de código
- [ ] `details` es JSON válido (puede ser `{}`)
- [ ] **Sin stack trace**
- [ ] **Sin credenciales**
- [ ] **Sin rutas del servidor**

---

## Resumen de Cobertura

| Categoría | Rutas | Casos PASS | Casos FAIL | Estado |
|-----------|-------|-----------|-----------|--------|
| **Venues** | 2 | ___ | ___ | ⬜ OK |
| **Artists** | 3 | ___ | ___ | ⬜ OK |
| **Auth** | 2 | ___ | ___ | ⬜ OK |
| **Users** | 3 | ___ | ___ | ⬜ OK |
| **Events** | 6 | ___ | ___ | ⬜ OK |
| **Tickets** | 6 | ___ | ___ | ⬜ OK |
| **Autorización** | Transversal | ___ | ___ | ⬜ OK |
| **Errores** | Transversal | ___ | ___ | ⬜ OK |
| **TOTAL** | **22 rutas / 57 requests** | **___** | **___** | **⬜ OK** |

---

## Política de autorización implementada

- Usuarios o tickets ajenos: **403** (`AccessDeniedException`).
- Evento borrador consultado por anónimo o USER: **404** para ocultar el recurso.
- Evento borrador consultado por STAFF o ADMIN: **200**.

Las expectativas de esta colección reflejan la implementación actual; no hay una opción pendiente por decidir.

---

## Checklist Final

- [ ] 22/22 rutas cubiertas por las 57 requests manuales de la colección
- [ ] Todos los casos positivos ejecutados
- [ ] Todos los casos negativos (400, 401, 403, 404, 409) ejecutados
- [ ] Validación de propiedad (IDOR), incluyendo verificaciones posteriores a denegaciones mutables
- [ ] ErrorResponse validado en todas las denegaciones
- [ ] Tokens copiados manualmente (sin scripts)
- [ ] Sin errores 5xx en fase funcional
- [ ] Bitácora completada con esperado/observado
- [ ] Matriz de cobertura rellenada
- [ ] Defectos (si hay) documentados

---

**Última actualización:** 2026-10-18
