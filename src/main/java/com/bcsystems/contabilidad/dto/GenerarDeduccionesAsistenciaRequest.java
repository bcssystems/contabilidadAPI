package com.bcsystems.contabilidad.dto;

import java.time.LocalDate;

public record GenerarDeduccionesAsistenciaRequest(
        Integer empleadoId,
        LocalDate inicio,
        LocalDate fin
) {
}
