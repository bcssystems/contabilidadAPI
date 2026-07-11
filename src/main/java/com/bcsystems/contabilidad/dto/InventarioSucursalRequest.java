package com.bcsystems.contabilidad.dto;

public record InventarioSucursalRequest(
        Integer idSucursal,
        Integer stock,
        Integer stockMinimo,
        Integer stockMaximo
) {}
