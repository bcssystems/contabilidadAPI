package com.bcsystems.contabilidad.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PrestamoEmpleadoUpdateRequest(
        BigDecimal montoPrincipal,
        BigDecimal tasaInteresPorcentaje,
        Integer numeroParcialidades,
        LocalDate fechaPrimerPago
) {
}
