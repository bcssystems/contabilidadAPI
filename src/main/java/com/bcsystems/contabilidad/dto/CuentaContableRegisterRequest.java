package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.en.TipoCuentaContable;

public record CuentaContableRegisterRequest(
        String codigo,
        String nombre,
        TipoCuentaContable tipoCuentaContable,
        Long cuentaPadreId
) {
}
