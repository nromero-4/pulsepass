-- ============================================================================
-- PulsePass — E2E Test Fixtures (seed-e2e.sql)
-- Preparación de datos de prueba para ejecución manual en Postman
-- ============================================================================
-- Ejecutar sobre BD: pulsepass_e2e
-- Esto crea venues, artistas, usuarios privilegiados e inactive user
-- Seguro para volver a ejecutar; no elimina registros existentes.

BEGIN;

-- ============================================================================
-- 1. VENUES ACTIVOS
-- ============================================================================
INSERT INTO venues (code, name, city, address, capacity, active) VALUES
('VEN-E2E-001', 'Auditorio Principal', 'Bogotá', 'Carrera 5 No. 26-33', 5000, true),
('VEN-E2E-002', 'Teatro Metropolitano', 'Medellín', 'Calle 42 No. 49-25', 1500, true)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    city = EXCLUDED.city,
    address = EXCLUDED.address,
    capacity = EXCLUDED.capacity,
    active = EXCLUDED.active;

-- Venue inactivo para casos de validación
INSERT INTO venues (code, name, city, address, capacity, active) VALUES
('VEN-E2E-INACTIVE', 'Sala Cerrada', 'Cali', 'Carrera 1 No. 0-0', 500, false)
ON CONFLICT (code) DO UPDATE SET
    name = EXCLUDED.name,
    city = EXCLUDED.city,
    address = EXCLUDED.address,
    capacity = EXCLUDED.capacity,
    active = EXCLUDED.active;

-- ============================================================================
-- 2. ARTISTAS ACTIVOS
-- ============================================================================
-- Ya están seeded en V2__insert_initial_artists.sql, pero verificamos:
-- INSERT INTO artists (stage_name, country, genre, active) VALUES
-- ('Solar Beat', 'Colombia', 'Electronic', true),
-- ('Neon Waves', 'Argentina', 'Synth Pop', true),
-- ('Caribbean Sound', 'Puerto Rico', 'Reggaeton', true),
-- ('Ocean Drive', 'Mexico', 'Indie Pop', true),
-- ('Digital Pulse', 'United States', 'Electronic', true);

-- Artista inactivo para casos de validación
INSERT INTO artists (stage_name, country, genre, active) VALUES
('Silent Echo', 'Canada', 'Ambient', false)
ON CONFLICT DO NOTHING;

-- ============================================================================
-- 3. USUARIO ADMIN (Provisionado externamente, no por registro público)
-- ============================================================================
-- Crear usuario sin contraseña directa (será asignada via UserCredential)
INSERT INTO users (username, email, active) VALUES
('admin_e2e', 'admin_e2e@example.test', true)
ON CONFLICT (username) DO NOTHING;

-- Crear perfil del admin
INSERT INTO user_profiles (first_name, last_name, phone, city, birth_date, user_id)
SELECT 'Admin', 'E2E', '+573101234567', 'Bogotá', '1985-01-01'::date, users.id
FROM users
WHERE users.username = 'admin_e2e'
  AND NOT EXISTS (
    SELECT 1 FROM user_profiles WHERE user_id = users.id
  )
ON CONFLICT DO NOTHING;

-- Hash BCrypt de la contraseña desechable "AdminE2E#Pass123".
INSERT INTO user_credentials (user_id, password_hash)
SELECT users.id, '$2a$10$2/1x3/PvtKg.5Sy88b.bgOXG5f.6dP1GmXgcM9zIrV9hUubQzVWgi'
FROM users
WHERE users.username = 'admin_e2e'
  AND NOT EXISTS (
    SELECT 1 FROM user_credentials WHERE user_id = users.id
  )
ON CONFLICT (user_id) DO NOTHING;

-- Asignar rol ADMIN
INSERT INTO user_roles (user_id, role)
SELECT users.id, 'ADMIN'
FROM users
WHERE users.username = 'admin_e2e'
  AND NOT EXISTS (
    SELECT 1 FROM user_roles WHERE user_id = users.id AND role = 'ADMIN'
  )
ON CONFLICT (user_id, role) DO NOTHING;

-- ============================================================================
-- 4. USUARIO STAFF (Provisionado externamente)
-- ============================================================================
INSERT INTO users (username, email, active) VALUES
('staff_e2e', 'staff_e2e@example.test', true)
ON CONFLICT (username) DO NOTHING;

INSERT INTO user_profiles (first_name, last_name, phone, city, birth_date, user_id)
SELECT 'Staff', 'E2E', '+573109876543', 'Medellín', '1990-05-15'::date, users.id
FROM users
WHERE users.username = 'staff_e2e'
  AND NOT EXISTS (
    SELECT 1 FROM user_profiles WHERE user_id = users.id
  )
ON CONFLICT DO NOTHING;

-- Hash BCrypt de la contraseña desechable "StaffE2E#Pass456".
INSERT INTO user_credentials (user_id, password_hash)
SELECT users.id, '$2a$10$k4ncOUk37APqpXWszI9Dl.b/UYAVWQaKE.X2DoaaPZBhSsayf1OoC'
FROM users
WHERE users.username = 'staff_e2e'
  AND NOT EXISTS (
    SELECT 1 FROM user_credentials WHERE user_id = users.id
  )
ON CONFLICT (user_id) DO NOTHING;

-- Asignar rol STAFF
INSERT INTO user_roles (user_id, role)
SELECT users.id, 'STAFF'
FROM users
WHERE users.username = 'staff_e2e'
  AND NOT EXISTS (
    SELECT 1 FROM user_roles WHERE user_id = users.id AND role = 'STAFF'
  )
ON CONFLICT (user_id, role) DO NOTHING;

-- ============================================================================
-- 5. USUARIO INACTIVO (para casos de denegación)
-- ============================================================================
INSERT INTO users (username, email, active) VALUES
('inactive_user_e2e', 'inactive_e2e@example.test', false)
ON CONFLICT (username) DO NOTHING;

INSERT INTO user_profiles (first_name, last_name, phone, city, birth_date, user_id)
SELECT 'Inactive', 'User', '+573115555555', 'Cali', '1992-12-25'::date, users.id
FROM users
WHERE users.username = 'inactive_user_e2e'
  AND NOT EXISTS (
    SELECT 1 FROM user_profiles WHERE user_id = users.id
  )
ON CONFLICT DO NOTHING;

-- Hash BCrypt de la contraseña desechable "InactiveE2E#Pass789".
INSERT INTO user_credentials (user_id, password_hash)
SELECT users.id, '$2a$10$HyCFW7pt4hGyeMJTRNsP0OY4OGkkrrroFva/CXuT6h0yZvuQEZpbO'
FROM users
WHERE users.username = 'inactive_user_e2e'
  AND NOT EXISTS (
    SELECT 1 FROM user_credentials WHERE user_id = users.id
  )
ON CONFLICT (user_id) DO NOTHING;

-- Asignar rol USER
INSERT INTO user_roles (user_id, role)
SELECT users.id, 'USER'
FROM users
WHERE users.username = 'inactive_user_e2e'
  AND NOT EXISTS (
    SELECT 1 FROM user_roles WHERE user_id = users.id AND role = 'USER'
  )
ON CONFLICT (user_id, role) DO NOTHING;

-- ============================================================================
-- VERIFICACIÓN FINAL
-- ============================================================================
-- Mostrar datos cargados
SELECT 'VENUES ACTIVOS' as categoria, COUNT(*) as cantidad FROM venues WHERE active = true
UNION ALL
SELECT 'ARTISTAS ACTIVOS', COUNT(*) FROM artists WHERE active = true
UNION ALL
SELECT 'USUARIOS (total)', COUNT(*) FROM users
UNION ALL
SELECT 'USUARIOS ADMIN', COUNT(*) FROM users u
  JOIN user_roles ur ON u.id = ur.user_id WHERE ur.role = 'ADMIN'
UNION ALL
SELECT 'USUARIOS STAFF', COUNT(*) FROM users u
  JOIN user_roles ur ON u.id = ur.user_id WHERE ur.role = 'STAFF'
UNION ALL
SELECT 'USUARIOS INACTIVOS', COUNT(*) FROM users WHERE active = false;

COMMIT;
