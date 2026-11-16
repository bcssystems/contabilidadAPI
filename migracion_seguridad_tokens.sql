-- Ejecutar con respaldo previo de la base contabilidad.
UPDATE token SET is_revoked = 1, is_expired = 1;

ALTER TABLE token
    ADD COLUMN IF NOT EXISTS token_hash VARCHAR(64) NULL,
    ADD COLUMN IF NOT EXISTS refresh_token_hash VARCHAR(64) NULL,
    ADD COLUMN IF NOT EXISTS session_family VARCHAR(36) NULL;

-- Ejecutar después de validar el despliegue; elimina JWT antiguos en texto plano.
ALTER TABLE token DROP COLUMN IF EXISTS token;
ALTER TABLE token DROP COLUMN IF EXISTS refresh_token;

CREATE UNIQUE INDEX ux_token_token_hash ON token (token_hash);
CREATE UNIQUE INDEX ux_token_refresh_token_hash ON token (refresh_token_hash);
