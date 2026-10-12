package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.en.PersonType;
import com.bcsystems.contabilidad.domain.en.Role;
import com.bcsystems.contabilidad.domain.en.Position;
import java.math.BigDecimal;

public record UserUpdateRequest(
        String nombre,
        String primerApellido,
        String segundoApellido,
        Role role,
        PersonType personType,
        Boolean activo,
        Position position,
        BigDecimal sueldoBase,
        BigDecimal sueldoSemanal,
        String password
) {
}
