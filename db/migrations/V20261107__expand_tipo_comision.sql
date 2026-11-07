-- Ejecutar después de respaldar la base de datos de producción.
-- Hibernate no siempre actualiza los valores de un ENUM nativo de MySQL con ddl-auto=update.
ALTER TABLE comision
    MODIFY COLUMN tipo_comision
    ENUM('VENTA', 'COBRANZA', 'BONO', 'REPARACION') NOT NULL;
