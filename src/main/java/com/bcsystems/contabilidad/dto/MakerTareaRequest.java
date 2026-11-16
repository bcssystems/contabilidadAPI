package com.bcsystems.contabilidad.dto;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
public record MakerTareaRequest(@NotBlank String nombre, String descripcion, String checklist,
                                @NotBlank String estado, @NotBlank String prioridad, @NotBlank String color,
                                LocalDate fechaFin, Integer proyectoId, List<Integer> usuarioIds,
                                List<Integer> tareasMencionadasIds, List<Integer> usuariosMencionadosIds) {}
