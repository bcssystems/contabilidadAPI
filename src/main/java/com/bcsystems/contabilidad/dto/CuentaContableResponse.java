package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.en.TipoCuentaContable;

import java.util.List;

public record CuentaContableResponse(
        Long id,
        String codigo,
        String nombre,
        TipoCuentaContable tipoCuentaContable,
        Boolean activa,
        Long cuentaPadreId,
        List<CuentaContableResponse> subcuentas
) {
}
