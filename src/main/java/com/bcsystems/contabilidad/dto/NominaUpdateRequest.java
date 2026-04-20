package com.bcsystems.contabilidad.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record NominaUpdateRequest(
        LocalDate fecha_nomina,
        BigDecimal sueldo,
        Long empleadoId,
        Long asientoId
) {
}
