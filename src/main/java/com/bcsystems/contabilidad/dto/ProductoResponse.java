package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.en.CategoriaProducto;

import java.time.LocalDateTime;
import java.util.List;

public record ProductoResponse(
    Integer idProducto,
    String sku,
    String nombre,
    String descripcion,
    String marca,
    String modelo,
    CategoriaProducto categoria,
    Double precio1,
    Double precio2,
    Double precio3,
    Double precio4,
    Boolean precioPersonalizado,
    Integer stockActual,
    Integer stockMinimo,
    Integer stockMaximo,
    Double costoPromedio,
    Boolean tieneVariantes,
    Integer idProductoPadre,
    String productoPadreNombre,
    Boolean activo,
    LocalDateTime fechaCreacion,
    LocalDateTime fechaActualizacion,
    List<ProductoMultimediaResponse> multimedia,
    List<InventarioSucursalResponse> inventarioSucursales,
    List<AtributoAsignadoResponse> atributosAsignados,
    List<ProductoResponse> variantes
) {}
