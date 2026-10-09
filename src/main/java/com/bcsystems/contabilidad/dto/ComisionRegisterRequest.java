package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.en.TipoComision;

import java.math.BigDecimal;

public record ComisionRegisterRequest(
        TipoComision tipoComision,
        String concepto,
        Long ventaId,
        Long usuarioId,
        BigDecimal montoBase,
        BigDecimal porcentajeComision,
        BigDecimal monto
) {
}
