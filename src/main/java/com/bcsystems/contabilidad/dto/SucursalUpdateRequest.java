package com.bcsystems.contabilidad.dto;

import jakarta.validation.constraints.NotBlank;

public record SucursalUpdateRequest(
    @NotBlank String nombre,
    String direccion,
    String telefono,
    Boolean activa
) {}
