package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.en.EstadoMovimiento;
import com.bcsystems.contabilidad.domain.en.TipoMovimiento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimientoAlmacenResponse(
        Long id,
        TipoMovimiento tipo,
        Long varianteId,
        String varianteNombre,
        String varianteNumeroSerie,
        String productoPadreNombre,
        String productoPadreSku,
        Integer cantidad,
        Integer solicitanteId,
        String solicitanteNombre,
        Integer aprobadorId,
        String aprobadorNombre,
        LocalDateTime fechaSolicitud,
        LocalDateTime fechaAprobacion,
        LocalDateTime fechaMovimiento,
        EstadoMovimiento estado,
        String observacion,
        String valeFolio,
        String folioVenta,
        String medioCompra,
        String proveedorNombre,
        BigDecimal precioCompra,
        String imei,
        String descripcion,
        Integer sucursalId,
        String sucursalNombre
) {}
