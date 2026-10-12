# Plan de cambios para el proyecto Bonds

## Contexto

Este documento contiene los cambios funcionales solicitados para el proyecto ubicado en:

```text
C:\AppDevelopment\bonds
```

No implementar estos cambios en `contabilidad` ni en `contabilidadFront`. El proyecto objetivo es `bonds`.

## Prioridad 1: Corregir ventas en espera y cobro

### Problema confirmado

Al cobrar una venta en espera, el flujo actual hace lo siguiente:

1. Actualiza la venta en espera.
2. Ejecuta `POST /ventas/{id}/cancelar-espera`.
3. Crea otra venta con `POST /ventas`.

Esto deja la venta original cancelada/en espera y crea una segunda venta completada con los mismos productos.

Archivos relevantes:

```text
frontend/js/modules/ventas.js
  confirmarCobro()
  finalizarEsperaActiva()
  guardarEsperaActiva()
  ponerEnEspera()
  cancelarEsperaLocal()

backend/bonds/src/main/java/com/bcsystems/bonds/service/impl/VentaServiceImpl.java
  ponerEnEspera()
  actualizarEspera()
  cancelarEspera()
  pagar()
```

### Comportamiento requerido

- Una venta debe conservar el mismo `idVenta` durante todo su ciclo.
- Al poner una venta en espera debe quedar en estado `ESPERA`.
- Al cobrar una venta en espera se debe actualizar esa misma venta a `COMPLETADA`.
- No se debe cancelar la venta original para crear otra.
- El cobro debe registrar pagos, caja e inventario una sola vez.
- El boton `Eliminar` solo debe operar si el estado real es `ESPERA`.
- Si la venta ya cambio de estado, el frontend debe recargar la lista y no mostrar un error incorrecto.

### Implementacion sugerida

1. Hacer que la creacion de una venta en espera cree directamente el estado `ESPERA`, preferentemente mediante un endpoint transaccional dedicado.
2. Cuando exista `state.activeEsperaId`, `confirmarCobro()` debe llamar al endpoint de pago de esa venta, no a `POST /ventas`.
3. Ajustar `pagar()` para aceptar una venta `ESPERA` y cambiarla a `COMPLETADA`.
4. Eliminar del flujo de cobro la llamada a `finalizarEsperaActiva()`.
5. No ejecutar `cancelar-espera` al cobrar.
6. Agregar bloqueo de doble clic en `Cobrar`, `Espera` y `Eliminar`.
7. Validar el estado anterior en backend dentro de una transaccion.
8. Usar bloqueo pesimista o una estrategia de idempotencia para evitar dos cobros simultaneos.
9. Corregir tambien el flujo de ventas a credito y de ventas impresas pendientes.

### Pruebas requeridas

- Venta nueva cobrada una vez: un solo registro `COMPLETADA`.
- Venta nueva puesta en espera: un solo registro `ESPERA`.
- Venta en espera reanudada y cobrada: el mismo `idVenta` debe terminar como `COMPLETADA`.
- Doble clic en cobrar: una sola venta y un solo registro de pago.
- Eliminar venta en espera: debe eliminar/cancelar correctamente.
- Eliminar una venta ya cobrada: debe recargar la lista y no intentar cancelarla como espera.
- Recargar el navegador entre poner en espera y cobrar.

## Prioridad 2: Modales de compras y movimientos de inventario

### Compras

- Revisar por que el boton `Agregar Producto` no abre su modal.
- Comparar el flujo con el modal de compras que ya funciona en Bonds.
- Verificar que el modulo y su script se carguen desde `dashboard.js`.
- Verificar que el boton tenga `type="button"`.
- Verificar que `bindEvents()` se ejecute despues de cargar `pages/compras.html`.
- El modal debe permitir agregar varias lineas de producto sin crecer indefinidamente.
- Cada linea debe indicar claramente:
  - Producto.
  - Cantidad.
  - Precio unitario.
  - Subtotal.
- El total debe recalcularse automaticamente.

### Movimientos de inventario

- Reutilizar el patron de compras para permitir varias lineas dentro de un mismo modal.
- Agregar scroll interno para que el modal no sea demasiado alto.
- Etiquetar todos los campos, sin depender unicamente del placeholder.
- Las etiquetas requeridas son:
  - Producto o variante.
  - Tipo de movimiento.
  - Cantidad.
  - Precio de compra.
  - Sucursal.
  - Referencia.
  - Vale o folio.
  - Observacion.
- Validar que cada linea tenga producto y cantidad valida antes de enviar.
- Enviar todas las lineas del modal de forma controlada y mostrar el resultado de cada operacion.

## Prioridad 3: Autorizacion de inventario

Regla requerida:

- Si el usuario no es administrador, cualquier movimiento que requiera autorizacion debe quedar como `PENDIENTE`.
- Un administrador puede registrar/aprobar directamente.
- Solo un administrador puede aprobar o rechazar solicitudes.
- El frontend debe mostrar `Solicitar autorizacion` a usuarios no administradores.
- El frontend debe mostrar `Registrar` o `Aprobar` a administradores.
- El backend debe validar el rol; no confiar solo en ocultar botones.
- Aprobar dos veces la misma solicitud debe ser rechazado de forma segura.

Revisar especialmente:

```text
frontend/js/modules/almacen.js
frontend/js/modules/productos.js
backend/bonds/src/main/java/com/bcsystems/bonds/controller/*Movimiento*
backend/bonds/src/main/java/com/bcsystems/bonds/service/*Movimiento*
```

## Prioridad 4: Incidencias de asistencia

Registrar y mostrar automaticamente:

- Fuera del punto geografico autorizado.
- Distancia respecto al punto autorizado.
- Minutos de retardo en la entrada.
- Minutos de salida temprana.

Requisitos:

- La incidencia de ubicacion debe conservarse en el registro de asistencia.
- El calculo de minutos debe usar el horario asignado del empleado.
- Mostrar los minutos en la tabla y en el detalle.
- La tolerancia configurada debe aplicarse consistentemente.
- Las deducciones deben ser idempotentes; sincronizar dos veces no debe duplicarlas.

## Prioridad 5: Sueldo por hora

- Guardar el sueldo por hora de cada usuario en la base de datos.
- Calcularlo como:

```text
sueldo semanal / horas semanales efectivas del horario
```

- Descontar breaks de las horas efectivas.
- Calcularlo automaticamente al:
  - Crear usuario.
  - Asignar horario.
  - Modificar horario.
  - Modificar sueldo semanal.
- Exponerlo en los DTO de usuarios.
- Mostrarlo en la tabla y detalle del usuario.
- Usarlo para calcular deducciones por asistencia.
- Agregar la columna correspondiente mediante Hibernate o migracion controlada.

## Prioridad 6: Nomina

- Al generar una nomina debe abrirse un modal para elegir imprimir.
- El modal debe ofrecer:
  - Ticket de 80 mm.
  - Tamano carta.
- El menu kebab de cada nomina debe incluir `Imprimir nomina`.
- La impresion debe usar el mismo PDF/recibo sin crear registros adicionales.

## Prioridad 7: Comisiones y filtros

- Las comisiones deben poder filtrarse por fecha desde/hasta.
- Todas las fechas deben tener etiquetas visibles `Desde` y `Hasta`.
- Los filtros de las cuatro pestanas de nomina deben organizarse en tres controles por fila en escritorio.
- En movil deben pasar a una sola columna.

## Prioridad 8: Ticket de venta

- Mostrar un modal antes de imprimir.
- Permitir elegir 80 mm o carta.
- Corregir el problema de imagen/logo que no se muestra correctamente.
- Quitar el logo por defecto si no existe una imagen configurada.
- No insertar una imagen rota ni un logo placeholder.
- Validar la impresion en navegador con popup permitido.
- Validar que el ticket muestre venta, detalle, pagos, totales, caja y folio.

## Validacion final

Backend:

```powershell
.\mvnw.cmd test -q
```

Frontend:

```powershell
node --check frontend/js/modules/ventas.js
node --check frontend/js/modules/compras.js
node --check frontend/js/modules/almacen.js
node --check frontend/js/modules/asistencia.js
node --check frontend/js/modules/nomina.js
git diff --check
```

Pruebas manuales obligatorias:

1. Crear compra con varias lineas.
2. Agregar y quitar lineas de inventario.
3. Ejecutar movimiento como administrador.
4. Ejecutar movimiento como no administrador.
5. Poner venta en espera y cobrarla despues.
6. Confirmar que solo exista un `idVenta`.
7. Intentar eliminar una venta que ya fue cobrada.
8. Imprimir ticket en 80 mm y carta.
9. Generar nomina e imprimir desde el modal y el kebab.

## Nota sobre los logs compartidos

Los logs con tablas `persona`, `rol`, `permiso` y el proceso `[bonds]` pertenecen a Bonds. No son logs del backend `contabilidad`, que utiliza entidades y rutas distintas. El problema de duplicacion debe corregirse en Bonds siguiendo principalmente la Prioridad 1.
