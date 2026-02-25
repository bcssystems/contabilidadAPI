package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.en.TipoComision;

import java.math.BigDecimal;

public record ComisionRegisterRequest(
        TipoComision tipoComision,
        Long ventaId,
        Long empleadoId,
        BigDecimal montoBase,
        BigDecimal porcentajeComision
) {
}
