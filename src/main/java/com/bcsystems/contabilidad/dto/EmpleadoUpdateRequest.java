package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.en.PersonType;
import com.bcsystems.contabilidad.domain.en.Position;

import java.math.BigDecimal;

public record EmpleadoUpdateRequest(
        String nombre,
        String primerApellido,
        String segundoApellido,
        PersonType personType,
        Position position,
        BigDecimal sueldoBase,
        Boolean activo
) {
}
