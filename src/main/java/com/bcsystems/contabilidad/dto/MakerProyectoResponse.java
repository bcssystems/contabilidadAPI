package com.bcsystems.contabilidad.dto;
import java.time.*;
import java.util.List;
public record MakerProyectoResponse(Integer id, String nombre, String descripcion, String estado, String prioridad,
                                    String color, LocalDate fechaFin, LocalDateTime fechaCreacion, Integer cotizacionId,
                                    String cotizacionFolio, List<MakerUserResponse> asignados, long tareasTotal,
                                    long tareasHechas) {}
