package com.bcsystems.contabilidad.dto;

public record ConfiguracionAsistenciaRequest(Integer toleranciaEntrada, Integer toleranciaSalida,
                                             Integer radioMaximo, Boolean requiereSelfie) { }
