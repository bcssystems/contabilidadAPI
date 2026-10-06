package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.Asistencia;
import com.bcsystems.contabilidad.domain.Marcaje;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AsistenciaTableroResponse(Integer id, LocalDate fecha, LocalDateTime entrada, LocalDateTime salida,
                                         String estado, String estadoUbicacion, Double distanciaMetros) {
    public static AsistenciaTableroResponse fromEntity(Asistencia attendance, Marcaje mark) {
        return new AsistenciaTableroResponse(attendance.getId(), attendance.getFecha(), attendance.getEntrada(), attendance.getSalida(),
                attendance.getEstado().name(), mark == null ? null : mark.getEstadoUbicacion().name(), mark == null ? null : mark.getDistanciaMetros());
    }
}
