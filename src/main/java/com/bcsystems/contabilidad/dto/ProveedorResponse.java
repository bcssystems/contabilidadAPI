package com.bcsystems.contabilidad.dto;

public record ProveedorResponse(
        Long id,
        String nombre,
        Long cuentaPorPagarId
) {
}
