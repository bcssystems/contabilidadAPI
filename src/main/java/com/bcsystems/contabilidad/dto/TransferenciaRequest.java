package com.bcsystems.contabilidad.dto;

public record TransferenciaRequest(
        Integer idSucursalOrigen,
        Integer idSucursalDestino,
        Integer cantidad,
        String referencia,
        String observacion
) {}
