package com.bcsystems.contabilidad.dto;

import jakarta.validation.constraints.NotBlank;

public record SucursalRegisterRequest(
    @NotBlank String nombre,
    String direccion,
    String telefono,
    Boolean activa
) {}
