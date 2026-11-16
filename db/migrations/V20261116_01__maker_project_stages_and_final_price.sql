-- Migración incremental para instalaciones donde V20261116__maker_module.sql
-- ya fue ejecutada antes de agregar las etapas nuevas y el precio final.

ALTER TABLE maker_cotizacion
    ADD COLUMN IF NOT EXISTS precio_cama_final DECIMAL(12,2),
    ADD COLUMN IF NOT EXISTS precio_pieza_final DECIMAL(12,2);

UPDATE maker_proyecto
SET estado = 'COTIZACION'
WHERE estado = 'SIN_COMENZAR';

UPDATE maker_proyecto
SET estado = 'PRODUCCION'
WHERE estado = 'EN_PROGRESO';

UPDATE maker_proyecto
SET estado = 'ENTREGA'
WHERE estado = 'HECHO';

-- Las cotizaciones existentes conservan el precio sugerido como precio final
-- hasta que el usuario realice un ajuste manual.
UPDATE maker_cotizacion
SET precio_cama_final = precio_cama,
    precio_pieza_final = precio_pieza
WHERE precio_cama_final IS NULL
   OR precio_pieza_final IS NULL;
