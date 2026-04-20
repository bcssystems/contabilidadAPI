package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.en.EstadoVenta;
import com.bcsystems.contabilidad.domain.en.TipoVenta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record VentaResponse(
        Long id,
        LocalDate fecha_venta,
        TipoVenta tipoVenta,
        EstadoVenta estadoVenta,
        BigDecimal total,
        BigDecimal costo,
        Long asientoId,
        List<ComisionResponse> comisiones
) {
}
