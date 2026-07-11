package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.VentaDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VentaDetalleRepository extends JpaRepository<VentaDetalle, Integer> {
    List<VentaDetalle> findByVentaIdVenta(Integer idVenta);
    void deleteByVentaIdVenta(Integer idVenta);
}
