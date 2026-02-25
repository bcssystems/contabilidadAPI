package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.AsientoContable;
import com.bcsystems.contabilidad.dto.AsientoContableRegisterRequest;
import com.bcsystems.contabilidad.dto.AsientoContableUpdateRequest;

import java.util.List;
import java.util.Optional;

public interface AsientoContableService {
    AsientoContable save(AsientoContableRegisterRequest request);
    List<AsientoContable> findAll();
    Optional<AsientoContable> findById(Long id);
    AsientoContable update(Long id, AsientoContableUpdateRequest request);
    void delete(Long id);
}
