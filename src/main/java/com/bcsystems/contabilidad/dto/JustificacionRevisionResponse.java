package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.en.EstadoJustificacion;
import java.time.LocalDateTime;

public record JustificacionRevisionResponse(Integer justificacionId, Integer asistenciaId,
                                            Integer empleadoId, String comentario,
                                            String nombreArchivo, LocalDateTime creadoEn,
                                            EstadoJustificacion estado) { }
