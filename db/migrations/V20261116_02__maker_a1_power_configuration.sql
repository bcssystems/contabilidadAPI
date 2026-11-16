-- Ajusta el consumo promedio usado por la calculadora Maker.
-- La documentación de Bambu Lab reporta 95 W promedio durante impresión PLA.
-- Solo actualiza el valor inicial anterior; respeta cualquier ajuste manual.
UPDATE maker_configuracion
SET valor = 0.095,
    unidad = 'kW',
    descripcion = 'Consumo promedio de Bambu Lab A1 durante impresión PLA (95 W)'
WHERE clave = 'CONSUMO_KW'
  AND valor = 0.10;
