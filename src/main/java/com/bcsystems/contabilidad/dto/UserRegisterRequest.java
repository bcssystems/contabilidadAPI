package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.en.PersonType;
import com.bcsystems.contabilidad.domain.en.Role;

public record UserRegisterRequest(
        String nombre,
        String primerApellido,
        String segundoApellido,
        PersonType personType,
        String username,
        String password,
        Role role
) {
}
