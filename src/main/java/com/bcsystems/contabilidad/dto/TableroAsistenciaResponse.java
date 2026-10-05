package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.Asistencia;
import com.bcsystems.contabilidad.domain.EmpleadoHorario;
import java.math.BigDecimal;
import java.util.List;

public record TableroAsistenciaResponse(List<Asistencia> asistencias, EmpleadoHorario horario,
                                         BigDecimal horasLaboralesSemanales, BigDecimal sueldoPorHora) { }
