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
        BigDecimal totalComisiones,
        BigDecimal totalDeducciones,
        EstadoNomina estado,
        String empleadoNombre,
        Long empleadoId,
        Long asientoId
) {
    public static NominaResponse fromEntity(Nomina nomina) {
        return new NominaResponse(
                nomina.getIdNomina(),
                nomina.getFecha_nomina(),
                nomina.getSueldo(),
                nomina.getSueldoBase(),
                nomina.getTotalComisiones(),
                nomina.getTotalDeducciones(),
                nomina.getEstado(),
                nomina.getEmpleado() != null ? nomina.getEmpleado().getPersona().getNombre() : null,
                nomina.getEmpleado() != null ? nomina.getEmpleado().getIdEmpleado().longValue() : null,
                nomina.getAsientoContable() != null ? nomina.getAsientoContable().getIdAsientoContable() : null
        );
    }
}
