package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.en.CategoriaProducto;
import com.bcsystems.contabilidad.domain.en.EstadoProducto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ProductoAdminResponse(
        Long id,
        String nombre,
        String marca,
        String modelo,
        BigDecimal precioCompra,
        BigDecimal precioVenta,
        EstadoProducto estado,
        CategoriaProducto categoria,
        List<String> imagenes,
        String descripcion,
        List<String> especificaciones,
        String mensajeWhatsapp,
        Boolean pagadoCaja,
        Boolean activo,
        LocalDateTime fechaCreacion
) {}
