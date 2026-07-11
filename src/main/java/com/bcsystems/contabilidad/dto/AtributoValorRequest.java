package com.bcsystems.contabilidad.dto;

public record AtributoValorRequest(
    Integer idValor,
    String valor,
    String codigoSku,
    Boolean activo
) {}
