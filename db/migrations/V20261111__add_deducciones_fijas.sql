CREATE TABLE IF NOT EXISTS deduccion_fija (
    id_deduccion_fija BIGINT NOT NULL AUTO_INCREMENT,
    concepto VARCHAR(200) NOT NULL,
    monto DECIMAL(12,2) NOT NULL,
    activo BIT NOT NULL DEFAULT 1,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NULL,
    PRIMARY KEY (id_deduccion_fija)
);

CREATE TABLE IF NOT EXISTS deduccion_fija_usuario (
    deduccion_fija_id BIGINT NOT NULL,
    usuario_id INT NOT NULL,
    PRIMARY KEY (deduccion_fija_id, usuario_id),
    CONSTRAINT fk_deduccion_fija_usuario_fija FOREIGN KEY (deduccion_fija_id) REFERENCES deduccion_fija(id_deduccion_fija),
    CONSTRAINT fk_deduccion_fija_usuario_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id_user)
);

ALTER TABLE deduccion ADD COLUMN IF NOT EXISTS deduccion_fija_id BIGINT NULL;
ALTER TABLE deduccion ADD COLUMN IF NOT EXISTS periodo_inicio DATE NULL;
ALTER TABLE deduccion ADD COLUMN IF NOT EXISTS periodo_fin DATE NULL;
ALTER TABLE deduccion ADD UNIQUE KEY uk_deduccion_fija_periodo (deduccion_fija_id, id_usuario, periodo_inicio, periodo_fin);
