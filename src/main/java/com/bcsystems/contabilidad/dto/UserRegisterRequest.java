package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.en.PersonType;

public record UserRegisterRequest(
        String nombre,
        String primerApellido,
        String segundoApellido,
        PersonType personType,
        String username,
        String password
) {
}
