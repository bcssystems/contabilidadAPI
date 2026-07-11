package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.en.CategoriaProducto;

import java.time.LocalDateTime;

public record ProductoPadreResponse(
        Long id,
        String sku,
        String nombre,
        String marca,
        String modelo,
        CategoriaProducto categoria,
        String descripcion,
        String mensajeWhatsapp,
        Boolean tieneVariantes,
        Boolean activo,
        LocalDateTime fechaCreacion,
        long totalVariantes,
        long variantesDisponibles,
        String costoPromedio
) {}
