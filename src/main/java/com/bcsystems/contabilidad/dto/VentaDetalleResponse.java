package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.VentaDetalle;

public record VentaDetalleResponse(
    Integer idVentaDetalle,
    String productoNombre,
    String descripcion,
    Integer cantidad,
    Double precioUnitario,
    Double subtotal
) {
    public static VentaDetalleResponse fromEntity(VentaDetalle detalle) {
        return new VentaDetalleResponse(
            detalle.getIdVentaDetalle(),
            detalle.getProducto() != null ? detalle.getProducto().getNombre() : null,
            detalle.getDescripcion(),
            detalle.getCantidad(),
            detalle.getPrecioUnitario(),
            detalle.getSubtotal()
        );
    }
}
