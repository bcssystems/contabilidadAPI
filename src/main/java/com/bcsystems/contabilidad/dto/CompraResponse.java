package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.en.EstadoCompra;
import com.bcsystems.contabilidad.domain.en.TipoCompra;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CompraResponse(
        Long id,
        LocalDate fecha_compra,
        BigDecimal total_compra,
        EstadoCompra estadoCompra,
        Long proveedorId,
        Long asientoId,
        TipoCompra tipoCompra
) {
}
