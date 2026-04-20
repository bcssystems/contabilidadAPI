package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Venta;
import com.bcsystems.contabilidad.dto.VentaRegisterRequest;
import com.bcsystems.contabilidad.dto.VentaResponse;
import com.bcsystems.contabilidad.dto.VentaUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

public interface VentaService {
    Venta save(VentaRegisterRequest request);
    List<Venta> findAll();
    Page<VentaResponse> findAllPageable(PageRequest pageRequest);
    Optional<Venta> findById(Long id);
    Venta update(Long id, VentaUpdateRequest request);
    void delete(Long id);
}
