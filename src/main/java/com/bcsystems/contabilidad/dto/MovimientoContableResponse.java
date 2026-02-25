package com.bcsystems.contabilidad.dto;

import java.math.BigDecimal;

public record MovimientoContableResponse(
        Long id,
        BigDecimal debe,
        BigDecimal haber,
        Long cuentaContableId,
        String cuentaNombre,
        String cuentaCodigo,
        Long asientoContableId
) {
}
