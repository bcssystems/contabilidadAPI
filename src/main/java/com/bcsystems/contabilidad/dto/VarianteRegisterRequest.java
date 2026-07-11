package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.en.EstadoProducto;
import com.bcsystems.contabilidad.domain.en.TipoCompraProducto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record VarianteRegisterRequest(
        Long productoPadreId,
        String nombre,
        String numeroSerie,
        LocalDate fechaEntrada,
        Integer porcentajeBateria,
        Long proveedorId,
        TipoCompraProducto tipoCompra,
        String ubicacion,
        String imei,
        EstadoProducto estado,
        BigDecimal precioCompra,
        BigDecimal precioPublico,
        BigDecimal precioConsigna,
        BigDecimal precioMayoreo,
        String observacion,
        Integer responsableId,
        Integer sucursalId,
        Boolean pagadoCaja,
        List<Integer> idAtributoValores,
        List<InventarioSucursalRequest> inventarios
) {}
