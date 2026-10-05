package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.Deduccion;
import com.bcsystems.contabilidad.domain.en.EstadoDeduccion;
import com.bcsystems.contabilidad.domain.en.OrigenDeduccion;
import com.bcsystems.contabilidad.domain.en.TipoDeduccion;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DeduccionResponse(
        Long id,
        Long empleadoId,
        String empleadoNombre,
        String concepto,
        BigDecimal monto,
        LocalDate fecha,
        EstadoDeduccion estado,
        TipoDeduccion tipo,
        OrigenDeduccion origen,
        Integer asistenciaId,
        Long corteCajaId,
        Long nominaId
) {
    public static DeduccionResponse fromEntity(Deduccion deduccion) {
        return new DeduccionResponse(
                deduccion.getIdDeduccion(),
                deduccion.getEmpleado() != null ? deduccion.getEmpleado().getIdEmpleado().longValue() : null,
                deduccion.getEmpleado() != null && deduccion.getEmpleado().getPersona() != null
                        ? deduccion.getEmpleado().getPersona().getNombre() : null,
                deduccion.getConcepto(),
                deduccion.getMonto(),
                deduccion.getFecha(),
                deduccion.getEstado(),
                deduccion.getTipo(),
                deduccion.getOrigen(),
                deduccion.getAsistencia() != null ? deduccion.getAsistencia().getId() : null,
                deduccion.getCorteCaja() != null ? deduccion.getCorteCaja().getIdCorte().longValue() : null,
                deduccion.getNomina() != null ? deduccion.getNomina().getIdNomina() : null
        );
    }
}
