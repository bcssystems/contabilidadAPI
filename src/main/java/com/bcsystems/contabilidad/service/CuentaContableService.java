package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.CuentaContable;
import com.bcsystems.contabilidad.dto.CuentaContableRegisterRequest;
import com.bcsystems.contabilidad.dto.CuentaContableUpdateRequest;

import java.util.List;
import java.util.Optional;

public interface CuentaContableService {
    CuentaContable save(CuentaContableRegisterRequest request);
    List<CuentaContable> findAll();
    Optional<CuentaContable> findById(Long id);
    CuentaContable update(Long id, CuentaContableUpdateRequest request);
    void delete(Long id);
}
