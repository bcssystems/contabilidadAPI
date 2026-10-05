package com.bcsystems.contabilidad.dto;

import java.math.BigDecimal;
import java.util.List;

public record NominaDesgloseResponse(
        Long usuarioId,
        String usuarioNombre,
        BigDecimal sueldoBase,
        BigDecimal sueldoSemanal,
        List<ComisionResponse> comisiones,
        List<DeduccionResponse> deducciones,
        BigDecimal totalComisiones,
        BigDecimal totalDeducciones
) {
}
