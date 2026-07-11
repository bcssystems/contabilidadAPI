package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.en.EstadoProducto;
import com.bcsystems.contabilidad.domain.en.StatusProducto;
import com.bcsystems.contabilidad.domain.en.TipoCompraProducto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record VarianteResponse(
        Long id,
        Long productoPadreId,
        String productoPadreNombre,
        String productoPadreSku,
        String nombre,
        String numeroSerie,
        LocalDate fechaEntrada,
        Integer porcentajeBateria,
        Long proveedorId,
        String proveedorNombre,
        TipoCompraProducto tipoCompra,
        String ubicacion,
        String imei,
        StatusProducto status,
        EstadoProducto estado,
        BigDecimal precioCompra,
        BigDecimal precioPublico,
        BigDecimal precioConsigna,
        BigDecimal precioMayoreo,
        String valeFolioSalida,
        String folioVenta,
        String observacion,
        Integer responsableId,
        String responsableNombre,
        Integer sucursalId,
        String sucursalNombre,
        List<String> imagenes,
        Boolean pagadoCaja,
        Boolean activo,
        LocalDateTime fechaCreacion,
        List<InventarioSucursalResponse> inventarios,
        List<AtributoAsignadoResponse> atributos
) {}
