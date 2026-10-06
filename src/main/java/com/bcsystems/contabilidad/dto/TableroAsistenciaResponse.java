package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.UsuarioHorario;
import java.math.BigDecimal;
import java.util.List;

    public record TableroAsistenciaResponse(List<AsistenciaTableroResponse> asistencias, UsuarioHorario horario,
                                         BigDecimal horasLaboralesSemanales, BigDecimal sueldoPorHora) { }
