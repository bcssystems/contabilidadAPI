package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Producto;
import com.bcsystems.contabilidad.dto.ProductoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface ProductoService {

    ProductoResponse save(Producto producto);
    Page<ProductoResponse> buscarConFiltros(String search, Boolean activo, Boolean tieneVariantes, Pageable pageable);
    ProductoResponse findById(Integer id);
    Page<ProductoResponse> findBySucursal(Integer idSucursal, Pageable pageable);
    ProductoResponse update(Integer id, Producto producto);
    void delete(Integer id);
    Map<String, Object> getStats();
    ProductoMultimediaService getMultimediaService();
    ProductoResponse registrarMovimientoStock(Integer idProducto, String tipoMovimiento, Integer cantidad, Integer idSucursal, String referencia, String observacion);
    ProductoResponse transferirStock(Integer idProducto, Integer idSucursalOrigen, Integer idSucursalDestino, Integer cantidad, String referencia, String observacion);
}
