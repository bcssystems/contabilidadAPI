package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.en.TipoMovimiento;
import com.bcsystems.contabilidad.dto.KardexUnificadoResponse;
import com.bcsystems.contabilidad.dto.MovimientoStockResponse;
import com.bcsystems.contabilidad.repository.MovimientoStockRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class KardexServiceImpl implements KardexService {

    private final MovimientoStockRepository movimientoStockRepository;

    public KardexServiceImpl(MovimientoStockRepository movimientoStockRepository) {
        this.movimientoStockRepository = movimientoStockRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovimientoStockResponse> listarMovimientos(Integer idProducto, Integer idSucursal,
                                                            TipoMovimiento tipo,
                                                            LocalDateTime fechaInicio, LocalDateTime fechaFin,
                                                            Pageable pageable) {
        return movimientoStockRepository.buscarConFiltros(idProducto, idSucursal, tipo, fechaInicio, fechaFin, pageable)
                .map(MovimientoStockResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<KardexUnificadoResponse> listarTodo(LocalDateTime fechaInicio, LocalDateTime fechaFin,
                                                    TipoMovimiento tipo, Pageable pageable) {
        return movimientoStockRepository.findAllFiltered(fechaInicio, fechaFin, tipo, pageable)
                .map(KardexUnificadoResponse::fromEntity);
    }
}
