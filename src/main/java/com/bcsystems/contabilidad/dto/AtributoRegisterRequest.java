package com.bcsystems.contabilidad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AtributoRegisterRequest(
    @NotBlank String nombre,
    @NotNull Boolean activo,
    List<AtributoValorRequest> valores
) {}
