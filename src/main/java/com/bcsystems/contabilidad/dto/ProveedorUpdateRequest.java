package com.bcsystems.contabilidad.dto;

public record ProveedorUpdateRequest(
        String nombre,
        Long cuentaPorPagarId
) {
}
