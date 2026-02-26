package com.bcsystems.contabilidad.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CompraRegisterRequest(
        LocalDate fecha_compra,
        BigDecimal total_compra,
        Long proveedorId,
        Long asientoId
) {
}
