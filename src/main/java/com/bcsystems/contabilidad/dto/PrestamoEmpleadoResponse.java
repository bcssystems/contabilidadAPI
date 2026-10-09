package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.PrestamoEmpleado;
import com.bcsystems.contabilidad.domain.en.EstadoPrestamo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PrestamoEmpleadoResponse(
        Long id,
        Integer usuarioId,
        String usuarioNombre,
        BigDecimal montoPrincipal,
        BigDecimal tasaInteresPorcentaje,
        Integer numeroParcialidades,
        LocalDateTime fechaCreacion,
        LocalDate fechaPrimerPago,
        EstadoPrestamo estado,
        List<PrestamoParcialidadResponse> parcialidades
) {
    public static PrestamoEmpleadoResponse fromEntity(PrestamoEmpleado p) {
        return new PrestamoEmpleadoResponse(p.getIdPrestamo(), p.getUsuario().getIdUser(),
                p.getUsuario().getPersona() != null ? p.getUsuario().getPersona().getNombre() : p.getUsuario().getUsername(),
                p.getMontoPrincipal(), p.getTasaInteresPorcentaje(), p.getNumeroParcialidades(), p.getFechaCreacion(),
                p.getFechaPrimerPago(),
                p.getEstado(), p.getParcialidades().stream().map(PrestamoParcialidadResponse::fromEntity).toList());
    }
}
