package com.bcsystems.contabilidad.dto;

import java.time.LocalDate;
import java.util.List;

public record NominaRegisterRequest(
        LocalDate fecha_nomina,
        Long empleadoId,
        List<Long> comisionIds,
        List<Long> deduccionIds
) {
}
