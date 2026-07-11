package com.bcsystems.contabilidad.dto;

public record AtributoValorResponse(
    Integer idValor,
    Integer idAtributo,
    String valor,
    String codigoSku,
    Boolean activo
) {}
