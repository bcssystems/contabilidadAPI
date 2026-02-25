package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.en.TipoAsientoContable;

import java.time.LocalDate;
import java.util.List;

public record AsientoContableResponse(
        Long id,
        LocalDate fecha,
        String referencia,
        String descripcion,
        TipoAsientoContable tipoAsientoContable,
        List<MovimientoContableResponse> movimientos
) {
}
