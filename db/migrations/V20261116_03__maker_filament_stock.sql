-- Stock actual de filamentos Maker en kilogramos.
ALTER TABLE maker_filamento
    ADD COLUMN IF NOT EXISTS stock_kg DECIMAL(12,3) NOT NULL DEFAULT 0;

-- Evita descontar dos veces el material de un proyecto al volver a moverlo de etapa.
ALTER TABLE maker_proyecto
    ADD COLUMN IF NOT EXISTS filamento_descontado BIT NOT NULL DEFAULT 0;
