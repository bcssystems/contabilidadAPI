package com.bcsystems.contabilidad.dto;

import java.math.BigDecimal;
import java.util.List;

public record NominaBatchResponse(List<NominaResponse> nominas, BigDecimal totalSueldoBase,
                                  BigDecimal totalComisiones, BigDecimal totalDeducciones,
                                  BigDecimal totalNeto) {
}
