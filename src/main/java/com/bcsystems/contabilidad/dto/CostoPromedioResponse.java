package com.bcsystems.contabilidad.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CostoPromedioResponse(
        Long productoPadreId,
        String sku,
        String nombre,
        BigDecimal costoPromedio,
        Integer cantidadTotal,
        LocalDateTime ultimaActualizacion
) {}
