package com.bcsystems.contabilidad.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record MakerFilamentoInput(@NotNull Integer filamentoId, @NotNull @DecimalMin("0.001") BigDecimal gramos) {}
