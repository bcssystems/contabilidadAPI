package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.en.EstadoComision;
import com.bcsystems.contabilidad.domain.en.TipoComision;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ComisionResponse(
        Long id,
        TipoComision tipoComision,
        BigDecimal montoBase,
        BigDecimal porcentaje,
        BigDecimal montoComision,
        EstadoComision estadoComision,
        LocalDate fechaGeneracion,
        LocalDate fechaPago,
        Long usuarioId,
        Long ventaId,
        String concepto,
        String usuarioNombre
) {
}
