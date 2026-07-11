package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.dto.MovimientoAlmacenRegisterRequest;
import com.bcsystems.contabilidad.dto.MovimientoAlmacenResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface AlmacenService {

    MovimientoAlmacenResponse registrarEntrada(MovimientoAlmacenRegisterRequest request, Integer usuarioId);

    MovimientoAlmacenResponse solicitarSalida(MovimientoAlmacenRegisterRequest request, Integer usuarioId);

    MovimientoAlmacenResponse aprobarSalida(Long movimientoId, Integer aprobadorId);

    MovimientoAlmacenResponse rechazarSalida(Long movimientoId, Integer aprobadorId);

    Page<MovimientoAlmacenResponse> findPendientes(PageRequest pageRequest);

    Page<MovimientoAlmacenResponse> findAllMovimientos(PageRequest pageRequest);

    MovimientoAlmacenResponse findById(Long id);

    byte[] generarPdfSalida(Long movimientoId);
}
