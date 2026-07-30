package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.Gasto;

import java.time.LocalDateTime;

public record GastoResponse(
    Integer idGasto,
    String cajaNombre,
    String descripcion,
    Double monto,
    String usuario,
    String estado,
    LocalDateTime fechaCreacion
) {
    public static GastoResponse fromEntity(Gasto gasto) {
        String nombreUsuario = gasto.getUsuario() != null
            ? gasto.getUsuario().getNombre() + " " + gasto.getUsuario().getPrimerApellido()
            : null;
        return new GastoResponse(
            gasto.getIdGasto(),
            gasto.getCaja() != null ? gasto.getCaja().getNombre() : null,
            gasto.getDescripcion(),
            gasto.getMonto(),
            nombreUsuario,
            gasto.getEstado() != null ? gasto.getEstado().name() : null,
            gasto.getFechaCreacion()
        );
    }
}
