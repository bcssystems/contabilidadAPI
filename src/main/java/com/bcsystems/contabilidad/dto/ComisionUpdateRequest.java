package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.en.EstadoComision;
import com.bcsystems.contabilidad.domain.en.TipoComision;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ComisionUpdateRequest(
        TipoComision tipoComision,
        String concepto,
        EstadoComision estadoComision,
        BigDecimal montoBase,
        BigDecimal porcentajeComision,
        BigDecimal montoComision,
        LocalDate fechaPago,
        BigDecimal monto
) {
}
