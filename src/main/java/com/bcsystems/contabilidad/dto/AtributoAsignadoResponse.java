package com.bcsystems.contabilidad.dto;

public record AtributoAsignadoResponse(
        Integer idAtributo,
        String nombreAtributo,
        Integer idValor,
        String nombreValor,
        String codigoSku
) {}
