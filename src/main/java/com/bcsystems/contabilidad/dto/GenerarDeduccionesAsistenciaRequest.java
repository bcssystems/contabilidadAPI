package com.bcsystems.contabilidad.dto;

import java.time.LocalDate;

public record GenerarDeduccionesAsistenciaRequest(
        Integer usuarioId,
        LocalDate inicio,
        LocalDate fin
) {
}
