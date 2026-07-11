package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.en.CategoriaProducto;

import java.util.List;

public record ProductoPadreUpdateRequest(
        String sku,
        String nombre,
        String marca,
        String modelo,
        CategoriaProducto categoria,
        String descripcion,
        String mensajeWhatsapp,
        Boolean tieneVariantes,
        Boolean activo,
        List<VarianteUpdateRequest> variantes
) {}
