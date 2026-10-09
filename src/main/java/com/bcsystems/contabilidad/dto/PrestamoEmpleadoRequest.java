package com.bcsystems.contabilidad.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PrestamoEmpleadoRequest(
        Integer usuarioId,
        BigDecimal montoPrincipal,
        BigDecimal tasaInteresPorcentaje,
        Integer numeroParcialidades,
        LocalDate fechaPrimerPago
) {
}
