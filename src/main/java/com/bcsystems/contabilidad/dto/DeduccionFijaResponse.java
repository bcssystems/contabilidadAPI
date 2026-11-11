package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.DeduccionFija;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DeduccionFijaResponse(
        Long id,
        String concepto,
        BigDecimal monto,
        Boolean activo,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        List<Integer> usuarioIds,
        List<String> usuarios
) {
    public static DeduccionFijaResponse fromEntity(DeduccionFija item) {
        return new DeduccionFijaResponse(
                item.getIdDeduccionFija(), item.getConcepto(), item.getMonto(), item.getActivo(),
                item.getFechaInicio(), item.getFechaFin(),
                item.getUsuarios().stream().map(u -> u.getIdUser()).toList(),
                item.getUsuarios().stream().map(u -> u.getPersona() == null ? u.getUsername() :
                        (u.getPersona().getNombre() == null ? u.getUsername() : u.getPersona().getNombre())).toList());
    }
}
