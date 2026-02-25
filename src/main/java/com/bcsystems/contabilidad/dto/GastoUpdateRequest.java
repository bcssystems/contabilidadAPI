package com.bcsystems.contabilidad.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GastoUpdateRequest(
        LocalDate fecha_gasto,
        String concepto,
        BigDecimal monto,
        Long cuentaContableId,
        Long asientoId
) {
}
