package com.bcsystems.contabilidad.dto;

import java.time.LocalDate;

public record NominaUpdateRequest(
        LocalDate fecha_nomina,
        LocalDate fechaInicio,
        LocalDate fechaFin
) {
}
