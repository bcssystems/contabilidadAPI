package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ProductoService {

    Producto save(Producto producto);
    Page<Producto> buscarConFiltros(String search, Boolean activo, Boolean tieneVariantes, Pageable pageable);
    Producto findById(Integer id);
    Page<Producto> findBySucursal(Integer idSucursal, Pageable pageable);
    Producto update(Integer id, Producto producto);
    void delete(Integer id);
    Map<String, Object> getStats();
    ProductoMultimediaService getMultimediaService();
    Producto registrarMovimientoStock(Integer idProducto, String tipoMovimiento, Integer cantidad, Integer idSucursal, String referencia, String observacion);
    Producto transferirStock(Integer idProducto, Integer idSucursalOrigen, Integer idSucursalDestino, Integer cantidad, String referencia, String observacion);
}
