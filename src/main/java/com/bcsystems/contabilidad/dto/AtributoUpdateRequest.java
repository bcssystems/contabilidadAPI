package com.bcsystems.contabilidad.dto;

public record AtributoUpdateRequest(
    String nombre,
    Boolean activo,
    java.util.List<AtributoValorRequest> valores
) {}
