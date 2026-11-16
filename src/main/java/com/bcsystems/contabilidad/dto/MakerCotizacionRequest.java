package com.bcsystems.contabilidad.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record MakerCotizacionRequest(
        String descripcion,
        @NotBlank String modo,
        @NotNull @DecimalMin("0.001") BigDecimal gramosNetos,
        @NotNull @DecimalMin("0.01") BigDecimal horasImpresion,
        @NotNull @Min(1) Integer piezasPorCama,
        @Min(0) Integer cambiosColor,
        @NotBlank String tipoPrecio,
        Integer proyectoId,
        @NotEmpty List<@Valid MakerFilamentoInput> filamentos,
        List<@Valid MakerExtraInput> extras) {}
