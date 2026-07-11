package com.bcsystems.contabilidad.dto;

public record InventarioSucursalResponse(
        Integer id,
        Integer idSucursal,
        String sucursalNombre,
        Integer stock,
        Integer stockMinimo,
        Integer stockMaximo
) {}
