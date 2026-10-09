package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.Nomina;
import com.bcsystems.contabilidad.domain.en.EstadoNomina;

import java.math.BigDecimal;
import java.time.LocalDate;

public record NominaResponse(
        Long id,
        LocalDate fecha_nomina,
        BigDecimal sueldo,
        BigDecimal sueldoBase,
        BigDecimal sueldoSemanal,
        BigDecimal totalComisiones,
        BigDecimal totalDeducciones,
        EstadoNomina estado,
        String usuarioNombre,
        Long usuarioId,
        Long asientoId
) {
    public static NominaResponse fromEntity(Nomina nomina) {
        return new NominaResponse(
                nomina.getIdNomina(),
                nomina.getFecha_nomina(),
                nomina.getSueldo(),
                nomina.getSueldoBase(),
                nomina.getUsuario() != null ? nomina.getUsuario().getSueldoSemanal() : null,
                nomina.getTotalComisiones(),
                nomina.getTotalDeducciones(),
                nomina.getEstado(),
                nomina.getUsuario() != null && nomina.getUsuario().getPersona() != null ? nomina.getUsuario().getPersona().getNombre() : null,
                nomina.getUsuario() != null ? nomina.getUsuario().getIdUser().longValue() : null,
                nomina.getAsientoContable() != null ? nomina.getAsientoContable().getIdAsientoContable() : null
        );
    }
}
