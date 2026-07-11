package com.bcsystems.contabilidad.dto;

import java.util.List;

public record AtributoResponse(
    Integer idAtributo,
    String nombre,
    Boolean activo,
    List<AtributoValorResponse> valores
) {}
