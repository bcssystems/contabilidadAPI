package com.bcsystems.contabilidad.dto;
import jakarta.validation.constraints.NotBlank;
public record MakerMaterialRequest(@NotBlank String nombre, Boolean activo) {}
