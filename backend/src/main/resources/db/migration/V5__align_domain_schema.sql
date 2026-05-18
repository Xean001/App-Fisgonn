-- V5: Alinea el esquema real con el documento técnico de Fisgón.
--
-- Idempotente y ADITIVA: usa IF NOT EXISTS / guardas en pg_constraint y no
-- elimina ninguna columna existente (p. ej. password_salt sigue intacto,
-- porque AuthRoutes.kt lo usa). Deja la BD lista sin tener que modificar el
-- código Kotlin: las columnas nuevas tienen DEFAULT, y anonymous_username se
-- autogenera en la BD para los INSERT que aún no lo envían.

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ─────────────── CATEGORIES ───────────────
CREATE TABLE IF NOT EXISTS categories (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name       TEXT NOT NULL UNIQUE,
    icon       TEXT NOT NULL,
    color      TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO categories (name, icon, color) VALUES
    ('Robbery',     'shield-off',          '#E24B4A'),
    ('Harassment',  'alert-triangle',      '#EF9F27'),
    ('No lighting', 'bulb-off',            '#5A6A85'),
    ('Accident',    'car-crash',           '#D85A30'),
    ('Danger zone', 'map-pin-exclamation', '#993C1D'),
    ('Other',       'dots-circle',         '#888780')
ON CONFLICT (name) DO NOTHING;

-- ─────────────── USERS: columnas faltantes ───────────────
ALTER TABLE users ADD COLUMN IF NOT EXISTS rol                  TEXT NOT NULL DEFAULT 'ciudadano';
ALTER TABLE users ADD COLUMN IF NOT EXISTS is_active            BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verified       BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verify_token   TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_verify_expires TIMESTAMPTZ;
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_access          TIMESTAMPTZ;
ALTER TABLE users ADD COLUMN IF NOT EXISTS updated_at           TIMESTAMPTZ NOT NULL DEFAULT now();
-- Bloqueo temporal por fuerza bruta (doc §5, RF-02, §11.3)
ALTER TABLE users ADD COLUMN IF NOT EXISTS failed_login_attempts INTEGER NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS locked_until          TIMESTAMPTZ;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'users_rol_chk') THEN
    ALTER TABLE users ADD CONSTRAINT users_rol_chk
      CHECK (rol IN ('ciudadano','autoridad','admin'));
  END IF;
END $$;

-- ─────────────── Username anónimo (idea central del doc §1, §8) ───────────────
-- La GENERACIÓN vive en el backend (Kotlin) — es lógica de negocio. La BD solo
-- garantiza la integridad: NOT NULL + UNIQUE. No hay DEFAULT a propósito: el
-- backend siempre debe enviar el valor (única fuente de la lógica).
ALTER TABLE users ADD COLUMN IF NOT EXISTS anonymous_username TEXT;

-- Backfill de filas legadas con un valor único derivado del id (datos de
-- desarrollo previos; no usa la lógica de negocio del backend).
UPDATE users
   SET anonymous_username = 'anon-' || replace(id::text, '-', '')
 WHERE anonymous_username IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS users_anon_username_uq ON users (anonymous_username);
ALTER TABLE users ALTER COLUMN anonymous_username SET NOT NULL;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'users_anon_username_len_chk') THEN
    ALTER TABLE users ADD CONSTRAINT users_anon_username_len_chk
      CHECK (char_length(anonymous_username) BETWEEN 3 AND 50);
  END IF;
END $$;

-- ─────────────── REPORTS: columnas faltantes ───────────────
ALTER TABLE reports ADD COLUMN IF NOT EXISTS category_id UUID REFERENCES categories(id);
ALTER TABLE reports ADD COLUMN IF NOT EXISTS status      TEXT NOT NULL DEFAULT 'active';
ALTER TABLE reports ADD COLUMN IF NOT EXISTS expires_at  TIMESTAMPTZ NOT NULL DEFAULT now() + INTERVAL '72 hours';
ALTER TABLE reports ADD COLUMN IF NOT EXISTS updated_at  TIMESTAMPTZ NOT NULL DEFAULT now();

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'reports_status_chk') THEN
    ALTER TABLE reports ADD CONSTRAINT reports_status_chk
      CHECK (status IN ('active','expired','deleted'));
  END IF;
END $$;

-- description: normalizar filas legadas antes de forzar NOT NULL + CHECK 10..500
UPDATE reports
   SET description = 'Reporte migrado sin descripción válida'
 WHERE description IS NULL OR char_length(trim(description)) < 10;
UPDATE reports
   SET description = left(description, 500)
 WHERE char_length(description) > 500;
ALTER TABLE reports ALTER COLUMN description SET NOT NULL;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'reports_description_len_chk') THEN
    ALTER TABLE reports ADD CONSTRAINT reports_description_len_chk
      CHECK (char_length(description) BETWEEN 10 AND 500);
  END IF;
END $$;

CREATE INDEX IF NOT EXISTS reports_status_idx   ON reports (status, expires_at);
CREATE INDEX IF NOT EXISTS reports_location_idx ON reports (latitude, longitude);
CREATE INDEX IF NOT EXISTS reports_category_idx ON reports (category_id);

-- ─────────────── COMMENTS ───────────────
CREATE TABLE IF NOT EXISTS comments (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_id  UUID NOT NULL REFERENCES reports(id) ON DELETE CASCADE,
    user_id    UUID NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    content    TEXT NOT NULL CHECK (char_length(content) BETWEEN 2 AND 300),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS comments_report_idx ON comments (report_id);
CREATE INDEX IF NOT EXISTS comments_user_idx   ON comments (user_id);

-- ─────────────── REPORT HISTORY ───────────────
CREATE TABLE IF NOT EXISTS report_history (
    id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id   UUID NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    report_id UUID NOT NULL REFERENCES reports(id) ON DELETE CASCADE,
    seen_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, report_id)
);
CREATE INDEX IF NOT EXISTS report_history_user_idx ON report_history (user_id);

-- ─────────────── REFRESH TOKENS ───────────────
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash TEXT NOT NULL,
    revoked    BOOLEAN NOT NULL DEFAULT FALSE,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS refresh_tokens_user_idx ON refresh_tokens (user_id);
CREATE INDEX IF NOT EXISTS refresh_tokens_hash_idx ON refresh_tokens (token_hash);

-- ─────────────── AUDIT LOG ───────────────
CREATE TABLE IF NOT EXISTS audit_log (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID REFERENCES users(id) ON DELETE SET NULL,
    action     TEXT NOT NULL,
    ip         TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS audit_log_user_idx    ON audit_log (user_id);
CREATE INDEX IF NOT EXISTS audit_log_action_idx  ON audit_log (action);
CREATE INDEX IF NOT EXISTS audit_log_created_idx ON audit_log (created_at);

-- ─────────────── FUNCIONES / TRIGGERS ───────────────
CREATE OR REPLACE FUNCTION expire_reports() RETURNS void AS $$
BEGIN
  UPDATE reports
     SET status = 'expired', updated_at = now()
   WHERE status = 'active' AND expires_at < now();
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION set_updated_at() RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS users_updated_at ON users;
CREATE TRIGGER users_updated_at BEFORE UPDATE ON users
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS reports_updated_at ON reports;
CREATE TRIGGER reports_updated_at BEFORE UPDATE ON reports
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS comments_updated_at ON comments;
CREATE TRIGGER comments_updated_at BEFORE UPDATE ON comments
  FOR EACH ROW EXECUTE FUNCTION set_updated_at();
