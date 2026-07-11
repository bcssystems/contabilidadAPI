package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Venta;
import com.bcsystems.contabilidad.domain.en.EstadoVenta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VentaService {
    Venta save(Venta venta, List<com.bcsystems.contabilidad.domain.VentaDetalle> detalles, List<com.bcsystems.contabilidad.domain.VentaPago> pagos);
    List<Venta> findAll();
    Page<Venta> buscarConFiltros(Integer idSucursal, Integer idCaja, EstadoVenta estado, LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);
    Optional<Venta> findById(Integer id);
    List<Venta> findByCaja(Integer idCaja);
    Venta cancelar(Integer id);
}
