package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.VentaPago;

public record VentaPagoResponse(
    Integer idVentaPago,
    String tipoPagoNombre,
    String referencia,
    Double monto
) {
    public static VentaPagoResponse fromEntity(VentaPago pago) {
        return new VentaPagoResponse(
            pago.getIdVentaPago(),
            pago.getTipoPago() != null ? pago.getTipoPago().getNombre() : null,
            pago.getReferencia(),
            pago.getMonto()
        );
    }
}
