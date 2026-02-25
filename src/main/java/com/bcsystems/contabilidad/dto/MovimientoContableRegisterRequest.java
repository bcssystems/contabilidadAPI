package com.bcsystems.contabilidad.dto;

import java.math.BigDecimal;

public record MovimientoContableRegisterRequest(
        BigDecimal debe,
        BigDecimal haber,
        Long cuentaContableId,
        Long asientoContableId
) {
}
