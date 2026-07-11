package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.en.EstadoProducto;
import com.bcsystems.contabilidad.domain.en.StatusProducto;
import com.bcsystems.contabilidad.domain.en.TipoCompraProducto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record VarianteUpdateRequest(
        Long id,
        String nombre,
        String numeroSerie,
        LocalDate fechaEntrada,
        Integer porcentajeBateria,
        Long proveedorId,
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
        Integer sucursalId,
        Boolean pagadoCaja,
        Boolean activo,
        List<String> imagenesEliminar,
        List<Integer> idAtributoValores,
        List<InventarioSucursalRequest> inventarios
) {}
