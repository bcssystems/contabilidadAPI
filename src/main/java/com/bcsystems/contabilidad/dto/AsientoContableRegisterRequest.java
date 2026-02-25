package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.en.TipoAsientoContable;

import java.time.LocalDate;
import java.util.List;

public record AsientoContableRegisterRequest(
        LocalDate fecha,
        String referencia,
        String descripcion,
        TipoAsientoContable tipoAsientoContable,
        List<MovimientoContableRequest> movimientos
) {
}
