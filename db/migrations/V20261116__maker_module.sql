-- Maker utiliza tablas nuevas; ddl-auto=update también las crea en instalaciones existentes.
-- Esta migración documenta el contrato para despliegues controlados.
CREATE TABLE IF NOT EXISTS maker_material (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  activo BIT NOT NULL,
  UNIQUE KEY uk_maker_material_nombre (nombre)
);

CREATE TABLE IF NOT EXISTS maker_filamento (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(150) NOT NULL,
  material_id INT NOT NULL,
  precio_kilo DECIMAL(12,2) NOT NULL,
  marca VARCHAR(100), color VARCHAR(80), activo BIT NOT NULL,
  CONSTRAINT fk_maker_filamento_material FOREIGN KEY (material_id) REFERENCES maker_material(id)
);

CREATE TABLE IF NOT EXISTS maker_configuracion (
  id INT AUTO_INCREMENT PRIMARY KEY,
  clave VARCHAR(80) NOT NULL,
  valor DECIMAL(12,4) NOT NULL,
  unidad VARCHAR(30) NOT NULL,
  descripcion VARCHAR(255),
  UNIQUE KEY uk_maker_config_clave (clave)
);

CREATE TABLE IF NOT EXISTS maker_cotizacion (
  id INT AUTO_INCREMENT PRIMARY KEY,
  folio VARCHAR(30) NOT NULL UNIQUE, fecha DATETIME NOT NULL, usuario_id INT, proyecto_id INT,
  descripcion VARCHAR(160), modo VARCHAR(20) NOT NULL,
  gramos_netos DECIMAL(12,3) NOT NULL, gramos_merma DECIMAL(12,3) NOT NULL, gramos_totales DECIMAL(12,3) NOT NULL,
  horas_impresion DECIMAL(10,2) NOT NULL, piezas_por_cama INT NOT NULL,
  consumo_kw DECIMAL(12,4) NOT NULL, costo_kwh DECIMAL(12,4) NOT NULL,
  costo_material DECIMAL(12,2) NOT NULL, costo_electricidad DECIMAL(12,2) NOT NULL, costo_extras DECIMAL(12,2) NOT NULL,
  costo_cama DECIMAL(12,2) NOT NULL, costo_pieza DECIMAL(12,2) NOT NULL,
  tipo_precio VARCHAR(20) NOT NULL, margen DECIMAL(6,4) NOT NULL,
  precio_cama DECIMAL(12,2) NOT NULL, precio_pieza DECIMAL(12,2) NOT NULL,
  precio_cama_final DECIMAL(12,2), precio_pieza_final DECIMAL(12,2),
  CONSTRAINT fk_maker_cotizacion_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id_user)
);

CREATE TABLE IF NOT EXISTS maker_cotizacion_filamento (
  id INT AUTO_INCREMENT PRIMARY KEY, cotizacion_id INT NOT NULL, filamento_id INT,
  nombre_filamento VARCHAR(150) NOT NULL, material VARCHAR(100), marca VARCHAR(100), color VARCHAR(80),
  precio_kilo DECIMAL(12,2) NOT NULL, gramos DECIMAL(12,3) NOT NULL, costo DECIMAL(12,2) NOT NULL,
  CONSTRAINT fk_maker_cf_cotizacion FOREIGN KEY (cotizacion_id) REFERENCES maker_cotizacion(id),
  CONSTRAINT fk_maker_cf_filamento FOREIGN KEY (filamento_id) REFERENCES maker_filamento(id)
);

CREATE TABLE IF NOT EXISTS maker_cotizacion_extra (
  id INT AUTO_INCREMENT PRIMARY KEY, cotizacion_id INT NOT NULL, nombre VARCHAR(150) NOT NULL,
  cantidad DECIMAL(10,2) NOT NULL, precio_unitario DECIMAL(12,2) NOT NULL, importe DECIMAL(12,2) NOT NULL,
  CONSTRAINT fk_maker_ce_cotizacion FOREIGN KEY (cotizacion_id) REFERENCES maker_cotizacion(id)
);

CREATE TABLE IF NOT EXISTS maker_proyecto (
  id INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(160) NOT NULL, descripcion TEXT,
  estado VARCHAR(20) NOT NULL, prioridad VARCHAR(20) NOT NULL, color VARCHAR(20) NOT NULL,
  fecha_fin DATE, fecha_creacion DATETIME NOT NULL, usuario_id INT, cotizacion_id INT,
  CONSTRAINT fk_maker_proyecto_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id_user),
  CONSTRAINT fk_maker_proyecto_cotizacion FOREIGN KEY (cotizacion_id) REFERENCES maker_cotizacion(id)
);

CREATE TABLE IF NOT EXISTS maker_proyecto_usuario (
  proyecto_id INT NOT NULL, usuario_id INT NOT NULL, PRIMARY KEY (proyecto_id, usuario_id),
  CONSTRAINT fk_maker_pu_proyecto FOREIGN KEY (proyecto_id) REFERENCES maker_proyecto(id),
  CONSTRAINT fk_maker_pu_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id_user)
);

ALTER TABLE maker_cotizacion ADD COLUMN IF NOT EXISTS proyecto_id INT;
ALTER TABLE maker_cotizacion ADD COLUMN IF NOT EXISTS precio_cama_final DECIMAL(12,2);
ALTER TABLE maker_cotizacion ADD COLUMN IF NOT EXISTS precio_pieza_final DECIMAL(12,2);
ALTER TABLE maker_cotizacion ADD CONSTRAINT fk_maker_cotizacion_proyecto
  FOREIGN KEY (proyecto_id) REFERENCES maker_proyecto(id);

CREATE TABLE IF NOT EXISTS maker_tarea (
  id INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(180) NOT NULL, descripcion TEXT, checklist TEXT,
  estado VARCHAR(20) NOT NULL, prioridad VARCHAR(20) NOT NULL, color VARCHAR(20) NOT NULL,
  fecha_fin DATE, fecha_creacion DATETIME NOT NULL, proyecto_id INT,
  CONSTRAINT fk_maker_tarea_proyecto FOREIGN KEY (proyecto_id) REFERENCES maker_proyecto(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS maker_tarea_usuario (
  tarea_id INT NOT NULL, usuario_id INT NOT NULL, PRIMARY KEY (tarea_id, usuario_id),
  CONSTRAINT fk_maker_tu_tarea FOREIGN KEY (tarea_id) REFERENCES maker_tarea(id) ON DELETE CASCADE,
  CONSTRAINT fk_maker_tu_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id_user)
);

CREATE TABLE IF NOT EXISTS maker_tarea_mencion (
  tarea_id INT NOT NULL, mencionada_id INT NOT NULL, PRIMARY KEY (tarea_id, mencionada_id),
  CONSTRAINT fk_maker_tm_tarea FOREIGN KEY (tarea_id) REFERENCES maker_tarea(id) ON DELETE CASCADE,
  CONSTRAINT fk_maker_tm_mencionada FOREIGN KEY (mencionada_id) REFERENCES maker_tarea(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS maker_notificacion (
  id INT AUTO_INCREMENT PRIMARY KEY, usuario_id INT NOT NULL, titulo VARCHAR(180) NOT NULL,
  mensaje VARCHAR(500) NOT NULL, tipo VARCHAR(30) NOT NULL, referencia_tipo VARCHAR(30), referencia_id INT,
  leida BIT NOT NULL, fecha DATETIME NOT NULL,
  CONSTRAINT fk_maker_notificacion_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id_user) ON DELETE CASCADE
);

UPDATE maker_proyecto SET estado = 'COTIZACION' WHERE estado = 'SIN_COMENZAR';
UPDATE maker_proyecto SET estado = 'PRODUCCION' WHERE estado = 'EN_PROGRESO';
UPDATE maker_proyecto SET estado = 'ENTREGA' WHERE estado = 'HECHO';
