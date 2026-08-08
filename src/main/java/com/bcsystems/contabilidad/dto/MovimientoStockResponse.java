package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.MovimientoStock;

import java.time.LocalDateTime;

public record MovimientoStockResponse(
    Integer idMovimiento,
    Integer idProducto,
    String productoSku,
    String productoNombre,
    Integer idSucursal,
    String sucursalNombre,
    String tipoMovimiento,
    Integer cantidad,
    Integer stockAnterior,
    Integer stockNuevo,
    String referencia,
    String usuario,
    String observacion,
    LocalDateTime fechaMovimiento
) {
    public static MovimientoStockResponse fromEntity(MovimientoStock m) {
        return new MovimientoStockResponse(
            m.getIdMovimiento(),
            m.getProducto() != null ? m.getProducto().getIdProducto() : null,
            m.getProducto() != null ? m.getProducto().getSku() : null,
            m.getProducto() != null ? m.getProducto().getNombre() : null,
            m.getSucursal() != null ? m.getSucursal().getIdSucursal() : null,
            m.getSucursal() != null ? m.getSucursal().getNombre() : "Global",
            m.getTipoMovimiento() != null ? m.getTipoMovimiento().name() : null,
            m.getCantidad(),
            m.getStockAnterior(),
            m.getStockNuevo(),
            m.getReferencia(),
            m.getUsuario(),
            m.getObservacion(),
            m.getFechaMovimiento()
        );
    }
}
