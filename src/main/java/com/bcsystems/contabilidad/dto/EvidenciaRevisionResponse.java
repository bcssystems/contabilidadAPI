package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.en.*;
import java.time.LocalDateTime;

public record EvidenciaRevisionResponse(Integer evidenciaId, Integer marcajeId, Integer asistenciaId,
                                        Integer empleadoId, String tipo, LocalDateTime marcadoEn,
                                        Double distanciaMetros, EstadoEvidencia estado) { }
