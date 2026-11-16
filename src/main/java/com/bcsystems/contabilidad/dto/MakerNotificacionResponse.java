package com.bcsystems.contabilidad.dto;
import java.time.LocalDateTime;
public record MakerNotificacionResponse(Integer id, String titulo, String mensaje, String tipo,
                                        String referenciaTipo, Integer referenciaId, Boolean leida,
                                        LocalDateTime fecha) {}
