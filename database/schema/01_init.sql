-- =============================================================
-- GamerMood 2.0 — Script inicial de base de datos
-- Base de datos: gamermood  |  Motor: PostgreSQL 15+
--
-- Ejecutar desde cero:
--   psql -U postgres -c "CREATE DATABASE gamermood;"
--   psql -U postgres -d gamermood -f 01_init.sql
-- =============================================================

-- -------------------------------------------------------------
-- FUNCIÓN AUXILIAR: actualiza updated_at automáticamente
-- -------------------------------------------------------------
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- -------------------------------------------------------------
-- 1. ROLES
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS roles (
    id   SERIAL      PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE   -- 'USER', 'ADMIN'
);

-- -------------------------------------------------------------
-- 2. USUARIOS
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id            SERIAL       PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    activo        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Trigger: actualiza updated_at en cada UPDATE de usuarios
CREATE OR REPLACE TRIGGER trg_usuarios_updated_at
    BEFORE UPDATE ON usuarios
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- -------------------------------------------------------------
-- 3. USUARIOS_ROLES  (N:M)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios_roles (
    usuario_id INTEGER NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    rol_id     INTEGER NOT NULL REFERENCES roles(id)    ON DELETE CASCADE,
    PRIMARY KEY (usuario_id, rol_id)
);

-- -------------------------------------------------------------
-- 4. SESIONES DE JUEGO
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sesiones_juego (
    id          SERIAL       PRIMARY KEY,
    usuario_id  INTEGER      NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    game        VARCHAR(150) NOT NULL,
    mood        VARCHAR(20)  NOT NULL CHECK (mood IN ('happy', 'neutral', 'sad')),
    intensity   SMALLINT     NOT NULL CHECK (intensity BETWEEN 1 AND 10),
    experience  TEXT,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Índice: listar sesiones de un usuario ordenadas por fecha
CREATE INDEX IF NOT EXISTS idx_sesiones_usuario_fecha
    ON sesiones_juego (usuario_id, created_at DESC);

-- -------------------------------------------------------------
-- 5. CATEGORÍAS DE RECOMENDACIÓN
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS categorias_recomendacion (
    id     SERIAL      PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL UNIQUE
);

-- -------------------------------------------------------------
-- 6. PROMPTS IA
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS prompts_ia (
    id        SERIAL       PRIMARY KEY,
    nombre    VARCHAR(100) NOT NULL UNIQUE,
    contenido TEXT         NOT NULL,
    activo    BOOLEAN      NOT NULL DEFAULT TRUE
);

-- -------------------------------------------------------------
-- 7. RUNBOOKS
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS runbooks (
    id          SERIAL       PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    descripcion TEXT,
    prompt_id   INTEGER REFERENCES prompts_ia(id) ON DELETE SET NULL
);

-- -------------------------------------------------------------
-- 8. RECOMENDACIONES GENERADAS
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS recomendaciones_generadas (
    id           SERIAL    PRIMARY KEY,
    sesion_id    INTEGER   NOT NULL REFERENCES sesiones_juego(id) ON DELETE CASCADE,
    categoria_id INTEGER   REFERENCES categorias_recomendacion(id) ON DELETE SET NULL,
    runbook_id   INTEGER   REFERENCES runbooks(id) ON DELETE SET NULL,
    contenido    TEXT      NOT NULL,
    fuente       VARCHAR(20) NOT NULL DEFAULT 'REGLAS' CHECK (fuente IN ('REGLAS', 'OPENAI')),
    created_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Índice: obtener recomendaciones de una sesión
CREATE INDEX IF NOT EXISTS idx_recomendaciones_sesion
    ON recomendaciones_generadas (sesion_id);

-- -------------------------------------------------------------
-- 9. FEEDBACK DE RECOMENDACIÓN
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS feedback_recomendacion (
    id               SERIAL    PRIMARY KEY,
    recomendacion_id INTEGER   NOT NULL REFERENCES recomendaciones_generadas(id) ON DELETE CASCADE,
    util             BOOLEAN   NOT NULL,
    comentario       TEXT,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Índice: consultar feedback de una recomendación
CREATE INDEX IF NOT EXISTS idx_feedback_recomendacion
    ON feedback_recomendacion (recomendacion_id);

-- -------------------------------------------------------------
-- 10. ESTADOS DE FLUJO
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS estados_flujo (
    id          SERIAL      PRIMARY KEY,
    nombre      VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(150)
);

-- -------------------------------------------------------------
-- 11. TRANSICIONES DE FLUJO
--     Registra cada cambio de estado de una sesión (log de trazabilidad).
--     El estado actual de una sesión es la transición más reciente.
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS transiciones_flujo (
    id              SERIAL    PRIMARY KEY,
    sesion_id       INTEGER   NOT NULL REFERENCES sesiones_juego(id) ON DELETE CASCADE,
    estado_desde_id INTEGER   REFERENCES estados_flujo(id),   -- NULL en la transición inicial
    estado_hasta_id INTEGER   NOT NULL REFERENCES estados_flujo(id),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Índice: historial de transiciones de una sesión
CREATE INDEX IF NOT EXISTS idx_transiciones_sesion
    ON transiciones_flujo (sesion_id, created_at DESC);

-- =============================================================
-- DATOS INICIALES
-- =============================================================

-- Roles
INSERT INTO roles (name) VALUES ('USER'), ('ADMIN')
    ON CONFLICT (name) DO NOTHING;

-- Categorías de recomendación
INSERT INTO categorias_recomendacion (nombre) VALUES
    ('Consejos emocionales'),
    ('Juegos recomendados'),
    ('Descanso'),
    ('Social'),
    ('Rendimiento')
    ON CONFLICT (nombre) DO NOTHING;

-- Estados del flujo de recomendación
INSERT INTO estados_flujo (nombre, descripcion) VALUES
    ('SESION_REGISTRADA',        'El usuario ha completado y guardado la sesión de juego'),
    ('SESION_CLASIFICADA',       'La sesión ha sido analizada y clasificada por mood e intensidad'),
    ('RECOMENDACION_GENERADA',   'Se han generado recomendaciones para la sesión'),
    ('FEEDBACK_RECIBIDO',        'El usuario ha valorado las recomendaciones'),
    ('FLUJO_CERRADO',            'El flujo ha finalizado con el usuario satisfecho'),
    ('REINTENTO_SOLICITADO',     'El usuario no estaba satisfecho y solicita nuevas recomendaciones')
    ON CONFLICT (nombre) DO NOTHING;
