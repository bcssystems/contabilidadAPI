package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.VentaPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface VentaPagoRepository extends JpaRepository<VentaPago, Integer> {
    List<VentaPago> findByVentaIdVenta(Integer idVenta);
    List<VentaPago> findByVentaIdVentaIn(Collection<Integer> ids);
    void deleteByVentaIdVenta(Integer idVenta);
}
