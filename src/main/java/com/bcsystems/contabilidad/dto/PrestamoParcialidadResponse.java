package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.PrestamoParcialidad;
import com.bcsystems.contabilidad.domain.en.EstadoParcialidad;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PrestamoParcialidadResponse(
        Long id,
        Integer numero,
        BigDecimal capital,
        BigDecimal interes,
        BigDecimal monto,
        BigDecimal saldo,
        LocalDate fechaVencimiento,
        EstadoParcialidad estado
) {
    public static PrestamoParcialidadResponse fromEntity(PrestamoParcialidad p) {
        return new PrestamoParcialidadResponse(p.getIdParcialidad(), p.getNumero(), p.getCapital(), p.getInteres(),
                p.getMonto(), p.getSaldo(), p.getFechaVencimiento(), p.getEstado());
    }
}
