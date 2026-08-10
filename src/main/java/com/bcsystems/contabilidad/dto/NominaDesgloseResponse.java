package com.bcsystems.contabilidad.dto;

import java.math.BigDecimal;
import java.util.List;

public record NominaDesgloseResponse(
        Long empleadoId,
        String empleadoNombre,
        BigDecimal sueldoBase,
        List<ComisionResponse> comisiones,
        List<DeduccionResponse> deducciones,
        BigDecimal totalComisiones,
        BigDecimal totalDeducciones
) {
}
