package com.bcsystems.contabilidad.dto;

import java.math.BigDecimal;

public record MovimientoContableRequest(
        BigDecimal debe,
        BigDecimal haber,
        Long cuentaContableId
) {
}
