package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.Venta;
import com.bcsystems.contabilidad.domain.en.EstadoVenta;
import com.bcsystems.contabilidad.domain.en.TipoVenta;

import java.time.LocalDateTime;
import java.util.List;

public record VentaResponse(
    Integer idVenta,
    String sucursalNombre,
    String cajaNombre,
    String clienteNombre,
    TipoVenta tipoVenta,
    EstadoVenta estado,
    Double subtotal,
    Double descuento,
    Double total,
    LocalDateTime fecha,
    List<VentaDetalleResponse> detalles,
    List<VentaPagoResponse> pagos
) {
    public static VentaResponse fromEntity(Venta venta, List<VentaDetalleResponse> detalles, List<VentaPagoResponse> pagos) {
        String clienteNombre = null;
        if (venta.getCliente() != null) {
            clienteNombre = venta.getCliente().getNombre()
                + (venta.getCliente().getApellidoPaterno() != null ? " " + venta.getCliente().getApellidoPaterno() : "");
        }

        String cajaNombre = venta.getCaja() != null ? venta.getCaja().getNombre() : null;
        String sucursalNombre = venta.getCaja() != null && venta.getCaja().getSucursal() != null
            ? venta.getCaja().getSucursal().getNombre()
            : null;

        return new VentaResponse(
            venta.getIdVenta(),
            sucursalNombre,
            cajaNombre,
            clienteNombre,
            venta.getTipoVenta(),
            venta.getEstado(),
            venta.getSubtotal(),
            venta.getDescuento(),
            venta.getTotal(),
            venta.getFecha(),
            detalles,
            pagos
        );
    }
}
