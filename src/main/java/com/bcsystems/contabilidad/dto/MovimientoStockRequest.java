package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.en.TipoMovimiento;

public record MovimientoStockRequest(
        TipoMovimiento tipoMovimiento,
        Integer cantidad,
        Integer idSucursal,
        String referencia,
        String observacion
) {}
