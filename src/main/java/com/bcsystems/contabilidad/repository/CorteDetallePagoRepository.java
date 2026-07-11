package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.CorteDetallePago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CorteDetallePagoRepository extends JpaRepository<CorteDetallePago, Integer> {
    List<CorteDetallePago> findByCorteIdCorte(Integer idCorte);
}
