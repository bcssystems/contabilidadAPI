package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.en.CategoriaProducto;
import com.bcsystems.contabilidad.domain.en.EstadoProducto;

import java.math.BigDecimal;
import java.util.List;

public record ProductoEcommerceResponse(
        Long id,
        String nombre,
        String marca,
        String modelo,
        BigDecimal precioVenta,
        EstadoProducto estado,
        CategoriaProducto categoria,
        List<String> imagenes,
        String descripcion,
        List<String> especificaciones,
        String mensajeWhatsapp
) {}
