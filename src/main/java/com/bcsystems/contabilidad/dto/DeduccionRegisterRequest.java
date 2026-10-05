package com.bcsystems.contabilidad.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DeduccionRegisterRequest(
        Long usuarioId,
        String concepto,
        BigDecimal monto,
        LocalDate fecha,
        Long corteCajaId
) {
}
