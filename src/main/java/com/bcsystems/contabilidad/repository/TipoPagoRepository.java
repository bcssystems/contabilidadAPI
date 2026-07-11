package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.TipoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TipoPagoRepository extends JpaRepository<TipoPago, Integer> {
    List<TipoPago> findByActivoTrue();
}
