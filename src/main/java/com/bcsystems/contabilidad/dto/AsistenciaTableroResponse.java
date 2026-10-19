package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.Asistencia;
import com.bcsystems.contabilidad.domain.Marcaje;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record AsistenciaTableroResponse(Integer id, LocalDate fecha, LocalDateTime entrada, LocalDateTime salida,
                                          String estado, String estadoUbicacion, Double distanciaMetros, String puntoNombre,
                                          Double latitud, Double longitud, Integer radioPermitido,
                                          Integer minutosRetardo, Integer minutosSalidaTemprana, Boolean fueraDePunto,
                                           List<AsistenciaBreakResponse> breaks, Boolean incompleta) {
    public static AsistenciaTableroResponse fromEntity(Asistencia attendance, Marcaje mark,
                                                       List<AsistenciaBreakResponse> breaks) {
        return new AsistenciaTableroResponse(attendance.getId(), attendance.getFecha(), attendance.getEntrada(), attendance.getSalida(),
                attendance.getEstado().name(), mark == null ? null : mark.getEstadoUbicacion().name(), mark == null ? null : mark.getDistanciaMetros(),
                mark == null || mark.getPuntoGeografico() == null ? null : mark.getPuntoGeografico().getNombre(),
                mark == null || mark.getLatitud() == null ? null : mark.getLatitud().doubleValue(),
                mark == null || mark.getLongitud() == null ? null : mark.getLongitud().doubleValue(),
                mark == null || mark.getPuntoGeografico() == null ? null : mark.getPuntoGeografico().getRadioMetros(),
                attendance.getMinutosRetardo(), attendance.getMinutosSalidaTemprana(), attendance.getFueraDePunto(), breaks,
                attendance.getEntrada() != null && attendance.getSalida() == null);
    }
}
