package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.en.TipoComision;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ComisionRegisterRequest(
        TipoComision tipoComision,
        String concepto,
        Long ventaId,
        Long usuarioId,
        BigDecimal montoBase,
        BigDecimal porcentajeComision,
         BigDecimal monto,
         LocalDate fechaComision
) {
}
