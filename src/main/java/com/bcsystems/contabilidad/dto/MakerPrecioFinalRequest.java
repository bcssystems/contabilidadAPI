package com.bcsystems.contabilidad.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record MakerPrecioFinalRequest(@NotNull @DecimalMin("0.00") BigDecimal precioPiezaFinal) {}
