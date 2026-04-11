package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.MovimientoContable;
import com.bcsystems.contabilidad.dto.MovimientoContableRegisterRequest;
import com.bcsystems.contabilidad.dto.MovimientoContableResponse;
import com.bcsystems.contabilidad.dto.MovimientoContableUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

public interface MovimientoContableService {
    MovimientoContable save(MovimientoContableRegisterRequest request);
    List<MovimientoContable> findAll();
    Page<MovimientoContableResponse> findAllPageable(PageRequest pageRequest);
    Optional<MovimientoContable> findById(Long id);
    MovimientoContable update(Long id, MovimientoContableUpdateRequest request);
    void delete(Long id);
    List<MovimientoContable> findByAsiento(Long asientoId);
    List<MovimientoContable> findByCuenta(Long cuentaId);
}
