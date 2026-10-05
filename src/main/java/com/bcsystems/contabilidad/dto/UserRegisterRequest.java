package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.en.PersonType;
import com.bcsystems.contabilidad.domain.en.Role;
import com.bcsystems.contabilidad.domain.en.Position;
import java.math.BigDecimal;

public record UserRegisterRequest(
        String nombre,
        String primerApellido,
        String segundoApellido,
        PersonType personType,
        String username,
        String password,
        Role role,
        Position position,
        BigDecimal sueldoBase,
        BigDecimal sueldoSemanal
) {
}
