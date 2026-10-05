package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.Empleado;
import com.bcsystems.contabilidad.domain.en.PersonType;
import com.bcsystems.contabilidad.domain.en.Position;

import java.math.BigDecimal;

public record EmpleadoResponse(
        Long id,
        String nombre,
        String primerApellido,
        String segundoApellido,
        PersonType personType,
        Position position,
        BigDecimal sueldoBase,
        BigDecimal sueldoSemanal,
        Boolean activo
) {
    public static EmpleadoResponse fromEntity(Empleado empleado) {
        return new EmpleadoResponse(
                empleado.getIdEmpleado() != null ? empleado.getIdEmpleado().longValue() : null,
                empleado.getPersona() != null ? empleado.getPersona().getNombre() : null,
                empleado.getPersona() != null ? empleado.getPersona().getPrimerApellido() : null,
                empleado.getPersona() != null ? empleado.getPersona().getSegundoApellido() : null,
                empleado.getPersona() != null ? empleado.getPersona().getPersonType() : null,
                empleado.getPosition(),
                empleado.getSueldoBase(),
                empleado.getSueldoSemanal(),
                empleado.getActivo()
        );
    }
}
