package com.bcsystems.contabilidad.dto;
import java.time.LocalDate;
public record AsignarHorarioRequest(Integer horarioId, LocalDate vigenteDesde, LocalDate vigenteHasta) { }
