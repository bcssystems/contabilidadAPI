package com.bcsystems.contabilidad.dto;

public record ProveedorRegisterRequest(
        String nombre,
        Long cuentaPorPagarId
) {
}
