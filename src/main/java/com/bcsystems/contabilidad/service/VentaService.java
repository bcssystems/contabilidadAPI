package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Persona;
import com.bcsystems.contabilidad.domain.Venta;
import com.bcsystems.contabilidad.domain.VentaDetalle;
import com.bcsystems.contabilidad.domain.VentaPago;
import com.bcsystems.contabilidad.domain.en.EstadoVenta;
import com.bcsystems.contabilidad.dto.VentaResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface VentaService {
    VentaResponse save(Venta venta, List<VentaDetalle> detalles, List<VentaPago> pagos, Persona vendedor);
    List<VentaResponse> findAll();
    Page<VentaResponse> buscarConFiltros(Integer idSucursal, Integer idCaja, EstadoVenta estado, LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable);
    VentaResponse findById(Integer id);
    List<VentaResponse> findByCaja(Integer idCaja);
    VentaResponse cancelar(Integer id);
}
