package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.en.TipoCuentaContable;

public record CuentaContableUpdateRequest(
        String codigo,
        String nombre,
        TipoCuentaContable tipoCuentaContable,
        Boolean activa,
        Long cuentaPadreId
) {
}
