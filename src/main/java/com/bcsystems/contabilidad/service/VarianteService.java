package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Variante;
import com.bcsystems.contabilidad.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VarianteService {

    Variante save(VarianteRegisterRequest request, List<MultipartFile> imagenesFiles);

    Page<VarianteResponse> findByProductoPadreId(Long padreId, PageRequest pageRequest);

    VarianteResponse findResponseById(Long id);

    Variante findById(Long id);

    Variante update(Long id, VarianteUpdateRequest request, List<MultipartFile> imagenesFiles);

    void delete(Long id);

    Page<VarianteResponse> findDisponibles(PageRequest pageRequest);

    List<Variante> findDisponiblesByPadreId(Long padreId);

    VarianteResponse registrarMovimientoStock(Long id, MovimientoStockRequest request);

    VarianteResponse transferirStock(Long id, TransferenciaRequest request);

    VarianteResponse actualizarStockSucursal(Long id, Integer idSucursal, Integer nuevoStock);
}
