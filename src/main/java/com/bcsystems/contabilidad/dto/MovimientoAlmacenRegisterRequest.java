package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.en.TipoMovimiento;

import java.math.BigDecimal;

public record MovimientoAlmacenRegisterRequest(
        TipoMovimiento tipo,
        Long varianteId,
        Integer cantidad,
        BigDecimal precioCompra,
        String observacion,
        String valeFolio,
        String folioVenta,
        String medioCompra,
        Integer sucursalId
) {}
