package com.bcsystems.contabilidad.dto;

import java.math.BigDecimal;

public record MovimientoContableUpdateRequest(
        BigDecimal debe,
        BigDecimal haber,
        Long cuentaContableId
) {
}
