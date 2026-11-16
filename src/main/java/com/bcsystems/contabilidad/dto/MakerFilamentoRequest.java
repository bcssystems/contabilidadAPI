package com.bcsystems.contabilidad.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record MakerFilamentoRequest(@NotBlank String nombre, @NotNull Integer materialId,
                                    @NotNull @DecimalMin("0.00") BigDecimal precioKilo,
                                    @DecimalMin("0.000") BigDecimal stockKg,
                                    String marca, String color, Boolean activo) {}
