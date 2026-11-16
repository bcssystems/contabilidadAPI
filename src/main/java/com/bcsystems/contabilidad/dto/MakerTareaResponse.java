package com.bcsystems.contabilidad.dto;
import java.time.*;
import java.util.List;
public record MakerTareaResponse(Integer id, String nombre, String descripcion, String checklist, String estado,
                                 String prioridad, String color, LocalDate fechaFin, LocalDateTime fechaCreacion,
                                 Integer proyectoId, String proyectoNombre, List<MakerUserResponse> asignados,
                                 List<MakerTareaMentionResponse> tareasMencionadas) {
    public record MakerTareaMentionResponse(Integer id, String nombre) {}
}
