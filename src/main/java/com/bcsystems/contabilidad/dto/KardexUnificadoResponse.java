package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.MovimientoStock;

import java.time.LocalDateTime;

public record KardexUnificadoResponse(
    Long id,
    LocalDateTime fecha,
    String tipo,
    String entidad,
    String detalle,
    String usuario,
    String referencia,
    Integer cantidad,
    Integer stockAnterior,
    Integer stockNuevo,
    String origen
) {
    public static KardexUnificadoResponse fromEntity(MovimientoStock m) {
        return new KardexUnificadoResponse(
            m.getIdMovimiento() != null ? m.getIdMovimiento().longValue() : null,
            m.getFechaMovimiento(),
            m.getTipoMovimiento() != null ? m.getTipoMovimiento().name() : null,
            m.getProducto() != null ? "PRODUCTO (" + m.getProducto().getSku() + ")" : "PRODUCTO",
            m.getObservacion() != null
                ? m.getObservacion()
                : (m.getTipoMovimiento() != null ? m.getTipoMovimiento() + " de " + m.getCantidad() + " unidades" : null),
            m.getUsuario(),
            m.getReferencia(),
            m.getCantidad(),
            m.getStockAnterior(),
            m.getStockNuevo(),
            "MOVIMIENTO"
        );
    }
}
