package com.bcsystems.contabilidad.dto;

import java.time.LocalDateTime;

public record UserDetailedResponse(
    Integer idUsuario,
    String username,
    String rol,
    String nombre,
    String primerApellido,
    String segundoApellido,
    String tipoPersona,
    Boolean activo,
    LocalDateTime fechaRegistro
) {
}
