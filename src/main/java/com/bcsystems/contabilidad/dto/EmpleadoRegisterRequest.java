package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.en.PersonType;
import com.bcsystems.contabilidad.domain.en.Position;

public record EmpleadoRegisterRequest(
        String nombre,
        String primerApellido,
        String segundoApellido,
        PersonType personType,
        Position position
) {
}
