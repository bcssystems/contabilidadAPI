package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.en.EstadoVenta;
import com.bcsystems.contabilidad.domain.en.TipoVenta;

import java.math.BigDecimal;
import java.time.LocalDate;

public record VentaRegisterRequest(
        LocalDate fecha_venta,
        TipoVenta tipoVenta,
        BigDecimal total,
        BigDecimal costo,
        Long asientoId
) {
}
