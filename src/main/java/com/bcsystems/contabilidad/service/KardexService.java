package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.en.TipoMovimiento;
import com.bcsystems.contabilidad.dto.KardexUnificadoResponse;
import com.bcsystems.contabilidad.dto.MovimientoStockResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface KardexService {
    Page<MovimientoStockResponse> listarMovimientos(Integer idProducto, Integer idSucursal,
                                                    TipoMovimiento tipo,
                                                    LocalDateTime fechaInicio, LocalDateTime fechaFin,
                                                    Pageable pageable);

    Page<KardexUnificadoResponse> listarTodo(LocalDateTime fechaInicio, LocalDateTime fechaFin,
                                             TipoMovimiento tipo, Pageable pageable);
}
