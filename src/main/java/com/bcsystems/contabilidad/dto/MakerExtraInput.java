package com.bcsystems.contabilidad.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record MakerExtraInput(@NotBlank String nombre, @NotNull @DecimalMin("0.01") BigDecimal cantidad,
                              @NotNull @DecimalMin("0.00") BigDecimal precioUnitario) {}
