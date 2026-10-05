package com.bcsystems.contabilidad.dto;
import java.math.BigDecimal;
public record PuntoGeograficoRequest(String nombre, BigDecimal latitud, BigDecimal longitud, Integer radioMetros, Boolean activo) { }
