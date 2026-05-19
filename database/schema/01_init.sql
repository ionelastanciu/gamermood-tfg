-- =============================================================
-- GamerMood - esquema inicial PostgreSQL
-- Alineada con las entidades JPA.
-- Este script se ejecuta automáticamente solo cuando el volumen
-- de PostgreSQL se crea por primera vez.
-- =============================================================

CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS usuarios_roles (
    usuario_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (usuario_id, role_id),
    CONSTRAINT fk_usuarios_roles_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_usuarios_roles_role
        FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS sesiones_juego (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    game VARCHAR(100) NOT NULL,
    mood VARCHAR(50) NOT NULL,
    intensity INTEGER NOT NULL CHECK (intensity BETWEEN 1 AND 10),
    experience VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_sesiones_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_sesiones_usuario_fecha
    ON sesiones_juego (usuario_id, created_at DESC);

CREATE TABLE IF NOT EXISTS recomendaciones (
    id BIGSERIAL PRIMARY KEY,
    sesion_id BIGINT NOT NULL UNIQUE,
    texto VARCHAR(1000) NOT NULL,
    fuente VARCHAR(20) NOT NULL DEFAULT 'REGLAS',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_recomendaciones_sesion
        FOREIGN KEY (sesion_id) REFERENCES sesiones_juego(id) ON DELETE CASCADE,
    CONSTRAINT chk_recomendaciones_fuente
        CHECK (fuente IN ('REGLAS', 'GROQ'))
);

CREATE INDEX IF NOT EXISTS idx_recomendaciones_sesion
    ON recomendaciones (sesion_id);

CREATE TABLE IF NOT EXISTS feedback_recomendacion (
    id BIGSERIAL PRIMARY KEY,
    recomendacion_id BIGINT NOT NULL UNIQUE,
    util BOOLEAN NOT NULL,
    comentario VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_feedback_recomendacion
        FOREIGN KEY (recomendacion_id) REFERENCES recomendaciones(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS transiciones_estado (
    id BIGSERIAL PRIMARY KEY,
    sesion_id BIGINT NOT NULL,
    estado_anterior VARCHAR(40),
    estado_nuevo VARCHAR(40) NOT NULL,
    motivo VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_transiciones_sesion
        FOREIGN KEY (sesion_id) REFERENCES sesiones_juego(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_transiciones_sesion
    ON transiciones_estado (sesion_id, created_at DESC);

INSERT INTO roles (nombre) VALUES ('ROLE_USER')
    ON CONFLICT (nombre) DO NOTHING;

INSERT INTO roles (nombre) VALUES ('ROLE_ADMIN')
    ON CONFLICT (nombre) DO NOTHING;

-- Normaliza claves foráneas en bases ya creadas con Hibernate antes de este script.
DO $$
DECLARE constraint_name text;
BEGIN
    SELECT c.conname INTO constraint_name
    FROM pg_constraint c
    JOIN pg_attribute a ON a.attrelid = c.conrelid AND a.attnum = ANY (c.conkey)
    WHERE c.conrelid = 'sesiones_juego'::regclass
      AND c.contype = 'f'
      AND a.attname = 'usuario_id';

    IF constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE sesiones_juego DROP CONSTRAINT %I', constraint_name);
    END IF;

    IF constraint_name IS NULL OR constraint_name <> 'fk_sesiones_usuario' THEN
        ALTER TABLE sesiones_juego DROP CONSTRAINT IF EXISTS fk_sesiones_usuario;
    END IF;
    ALTER TABLE sesiones_juego
        ADD CONSTRAINT fk_sesiones_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE;
END $$;

ALTER TABLE recomendaciones
    DROP CONSTRAINT IF EXISTS chk_recomendaciones_fuente;

UPDATE recomendaciones
SET fuente = 'REGLAS'
WHERE fuente NOT IN ('REGLAS', 'GROQ');

ALTER TABLE recomendaciones
    ADD CONSTRAINT chk_recomendaciones_fuente
    CHECK (fuente IN ('REGLAS', 'GROQ'));

DO $$
DECLARE constraint_name text;
BEGIN
    SELECT c.conname INTO constraint_name
    FROM pg_constraint c
    JOIN pg_attribute a ON a.attrelid = c.conrelid AND a.attnum = ANY (c.conkey)
    WHERE c.conrelid = 'recomendaciones'::regclass
      AND c.contype = 'f'
      AND a.attname = 'sesion_id';

    IF constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE recomendaciones DROP CONSTRAINT %I', constraint_name);
    END IF;

    IF constraint_name IS NULL OR constraint_name <> 'fk_recomendaciones_sesion' THEN
        ALTER TABLE recomendaciones DROP CONSTRAINT IF EXISTS fk_recomendaciones_sesion;
    END IF;
    ALTER TABLE recomendaciones
        ADD CONSTRAINT fk_recomendaciones_sesion
        FOREIGN KEY (sesion_id) REFERENCES sesiones_juego(id) ON DELETE CASCADE;
END $$;

DO $$
DECLARE constraint_name text;
BEGIN
    SELECT c.conname INTO constraint_name
    FROM pg_constraint c
    JOIN pg_attribute a ON a.attrelid = c.conrelid AND a.attnum = ANY (c.conkey)
    WHERE c.conrelid = 'feedback_recomendacion'::regclass
      AND c.contype = 'f'
      AND a.attname = 'recomendacion_id';

    IF constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE feedback_recomendacion DROP CONSTRAINT %I', constraint_name);
    END IF;

    IF constraint_name IS NULL OR constraint_name <> 'fk_feedback_recomendacion' THEN
        ALTER TABLE feedback_recomendacion DROP CONSTRAINT IF EXISTS fk_feedback_recomendacion;
    END IF;
    ALTER TABLE feedback_recomendacion
        ADD CONSTRAINT fk_feedback_recomendacion
        FOREIGN KEY (recomendacion_id) REFERENCES recomendaciones(id) ON DELETE CASCADE;
END $$;

DO $$
DECLARE constraint_name text;
BEGIN
    SELECT c.conname INTO constraint_name
    FROM pg_constraint c
    JOIN pg_attribute a ON a.attrelid = c.conrelid AND a.attnum = ANY (c.conkey)
    WHERE c.conrelid = 'transiciones_estado'::regclass
      AND c.contype = 'f'
      AND a.attname = 'sesion_id';

    IF constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE transiciones_estado DROP CONSTRAINT %I', constraint_name);
    END IF;

    IF constraint_name IS NULL OR constraint_name <> 'fk_transiciones_sesion' THEN
        ALTER TABLE transiciones_estado DROP CONSTRAINT IF EXISTS fk_transiciones_sesion;
    END IF;
    ALTER TABLE transiciones_estado
        ADD CONSTRAINT fk_transiciones_sesion
        FOREIGN KEY (sesion_id) REFERENCES sesiones_juego(id) ON DELETE CASCADE;
END $$;

DO $$
DECLARE constraint_name text;
BEGIN
    SELECT c.conname INTO constraint_name
    FROM pg_constraint c
    JOIN pg_attribute a ON a.attrelid = c.conrelid AND a.attnum = ANY (c.conkey)
    WHERE c.conrelid = 'usuarios_roles'::regclass
      AND c.contype = 'f'
      AND a.attname = 'usuario_id';

    IF constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE usuarios_roles DROP CONSTRAINT %I', constraint_name);
    END IF;

    IF constraint_name IS NULL OR constraint_name <> 'fk_usuarios_roles_usuario' THEN
        ALTER TABLE usuarios_roles DROP CONSTRAINT IF EXISTS fk_usuarios_roles_usuario;
    END IF;
    ALTER TABLE usuarios_roles
        ADD CONSTRAINT fk_usuarios_roles_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE;
END $$;

DO $$
DECLARE constraint_name text;
BEGIN
    SELECT c.conname INTO constraint_name
    FROM pg_constraint c
    JOIN pg_attribute a ON a.attrelid = c.conrelid AND a.attnum = ANY (c.conkey)
    WHERE c.conrelid = 'usuarios_roles'::regclass
      AND c.contype = 'f'
      AND a.attname = 'role_id';

    IF constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE usuarios_roles DROP CONSTRAINT %I', constraint_name);
    END IF;

    IF constraint_name IS NULL OR constraint_name <> 'fk_usuarios_roles_role' THEN
        ALTER TABLE usuarios_roles DROP CONSTRAINT IF EXISTS fk_usuarios_roles_role;
    END IF;
    ALTER TABLE usuarios_roles
        ADD CONSTRAINT fk_usuarios_roles_role
        FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE;
END $$;
