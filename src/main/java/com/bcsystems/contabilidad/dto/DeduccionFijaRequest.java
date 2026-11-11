package com.bcsystems.contabilidad.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DeduccionFijaRequest(
        String concepto,
        BigDecimal monto,
        Boolean activo,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        List<Integer> usuarioIds
) {}
