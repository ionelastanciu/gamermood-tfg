-- =============================================================
-- GamerMood 2.0 — Script inicial de base de datos
-- Base de datos: gamermood  |  Motor: PostgreSQL 15+
--
-- TODO (Mario): revisar y ajustar tipos, restricciones y relaciones
-- conforme se valide el modelo ER definitivo.
-- =============================================================

-- Extensión para UUIDs (opcional, por si se decide usar UUID como PK)
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- -------------------------------------------------------------
-- 1. ROLES
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS roles (
    id   SERIAL PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE   -- 'USER', 'ADMIN'
);

-- -------------------------------------------------------------
-- 2. USUARIOS
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id            SERIAL PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    activo        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

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
    id          SERIAL PRIMARY KEY,
    usuario_id  INTEGER      NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    game        VARCHAR(150) NOT NULL,
    mood        VARCHAR(20)  NOT NULL CHECK (mood IN ('happy', 'neutral', 'sad')),
    intensity   SMALLINT     NOT NULL CHECK (intensity BETWEEN 1 AND 10),
    experience  TEXT,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- -------------------------------------------------------------
-- 5. CATEGORÍAS DE RECOMENDACIÓN
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS categorias_recomendacion (
    id     SERIAL PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL UNIQUE
);

-- -------------------------------------------------------------
-- 6. PROMPTS IA
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS prompts_ia (
    id        SERIAL PRIMARY KEY,
    nombre    VARCHAR(100) NOT NULL UNIQUE,
    contenido TEXT         NOT NULL,
    activo    BOOLEAN      NOT NULL DEFAULT TRUE
);

-- -------------------------------------------------------------
-- 7. RUNBOOKS
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS runbooks (
    id          SERIAL PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    descripcion TEXT,
    prompt_id   INTEGER REFERENCES prompts_ia(id)
);

-- -------------------------------------------------------------
-- 8. RECOMENDACIONES GENERADAS
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS recomendaciones_generadas (
    id           SERIAL PRIMARY KEY,
    sesion_id    INTEGER NOT NULL REFERENCES sesiones_juego(id) ON DELETE CASCADE,
    categoria_id INTEGER REFERENCES categorias_recomendacion(id),
    contenido    TEXT    NOT NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

-- -------------------------------------------------------------
-- 9. FEEDBACK DE RECOMENDACIÓN
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS feedback_recomendacion (
    id                   SERIAL PRIMARY KEY,
    recomendacion_id     INTEGER NOT NULL REFERENCES recomendaciones_generadas(id) ON DELETE CASCADE,
    util                 BOOLEAN NOT NULL,
    comentario           TEXT,
    created_at           TIMESTAMP NOT NULL DEFAULT NOW()
);

-- -------------------------------------------------------------
-- 10. ESTADOS DE FLUJO
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS estados_flujo (
    id     SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

-- -------------------------------------------------------------
-- 11. TRANSICIONES DE FLUJO
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS transiciones_flujo (
    id           SERIAL PRIMARY KEY,
    sesion_id    INTEGER NOT NULL REFERENCES sesiones_juego(id) ON DELETE CASCADE,
    estado_id    INTEGER NOT NULL REFERENCES estados_flujo(id),
    created_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

-- -------------------------------------------------------------
-- DATOS INICIALES
-- -------------------------------------------------------------
INSERT INTO roles (name) VALUES ('USER'), ('ADMIN')
    ON CONFLICT (name) DO NOTHING;

INSERT INTO categorias_recomendacion (nombre)
    VALUES ('Consejos emocionales'), ('Juegos recomendados'), ('Descanso'), ('Social')
    ON CONFLICT (nombre) DO NOTHING;

INSERT INTO estados_flujo (nombre)
    VALUES ('SESION_REGISTRADA'), ('RECOMENDACION_GENERADA'), ('FEEDBACK_RECIBIDO')
    ON CONFLICT (nombre) DO NOTHING;
