package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.en.PersonType;
import com.bcsystems.contabilidad.domain.en.Position;

import java.math.BigDecimal;

public record EmpleadoRegisterRequest(
        String nombre,
        String primerApellido,
        String segundoApellido,
        PersonType personType,
        Position position,
        BigDecimal sueldoBase,
        BigDecimal sueldoSemanal
) {
    public EmpleadoRegisterRequest(String nombre, String primerApellido, String segundoApellido, PersonType personType, Position position, BigDecimal sueldoBase) {
        this(nombre, primerApellido, segundoApellido, personType, position, sueldoBase, sueldoBase);
    }
}
