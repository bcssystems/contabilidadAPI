package com.bcsystems.contabilidad.dto;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
public record MakerProyectoRequest(@NotBlank String nombre, String descripcion, @NotBlank String estado,
                                   @NotBlank String prioridad, @NotBlank String color, @FutureOrPresent LocalDate fechaFin,
                                   Integer cotizacionId, List<Integer> usuarioIds) {}
