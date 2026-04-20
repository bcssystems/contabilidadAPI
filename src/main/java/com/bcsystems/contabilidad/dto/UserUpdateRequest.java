package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.en.PersonType;
import com.bcsystems.contabilidad.domain.en.Role;

public record UserUpdateRequest(
        String nombre,
        String primerApellido,
        String segundoApellido,
        Role role,
        PersonType personType,
        Boolean activo
) {
}
