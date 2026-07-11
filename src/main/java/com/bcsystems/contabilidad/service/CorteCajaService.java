package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.CorteCaja;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.Map;

public interface CorteCajaService {
    Page<CorteCaja> buscarConFiltros(Integer idSucursal, Integer idCaja, LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);
    CorteCaja findById(Integer id);
    CorteCaja realizarCorte(Integer idCaja, Integer idUsuario);
    Map<String, Object> previewCorte(Integer idCaja);
}
