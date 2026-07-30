package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.Caja;
import com.bcsystems.contabilidad.domain.en.CajaEstado;

import java.time.LocalDateTime;

public record CajaResponse(
    Integer idCaja,
    String nombre,
    Integer idSucursal,
    String sucursalNombre,
    CajaEstado estado,
    Double saldoActual,
    LocalDateTime fechaApertura,
    LocalDateTime fechaCierre,
    Boolean activa
) {
    public static CajaResponse fromEntity(Caja caja) {
        return new CajaResponse(
            caja.getIdCaja(),
            caja.getNombre(),
            caja.getSucursal() != null ? caja.getSucursal().getIdSucursal() : null,
            caja.getSucursal() != null ? caja.getSucursal().getNombre() : null,
            caja.getEstado(),
            caja.getSaldoActual(),
            caja.getFechaApertura(),
            caja.getFechaCierre(),
            caja.getActiva()
        );
    }
}
