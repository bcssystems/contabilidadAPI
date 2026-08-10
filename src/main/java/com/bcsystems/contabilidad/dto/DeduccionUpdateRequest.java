package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.en.EstadoDeduccion;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DeduccionUpdateRequest(
        String concepto,
        BigDecimal monto,
        LocalDate fecha,
        Long corteCajaId,
        EstadoDeduccion estado
) {
}
