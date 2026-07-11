package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.en.EstadoVenta;
import com.bcsystems.contabilidad.domain.en.TipoVenta;

import java.math.BigDecimal;
import java.time.LocalDate;

public record VentaUpdateRequest(
        LocalDate fecha_venta,
        TipoVenta tipoVenta,
        EstadoVenta estadoVenta,
        BigDecimal total,
        BigDecimal costo,
        Long asientoId,
        Integer sucursalId
) {
}
