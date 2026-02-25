package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    Optional<Venta> findByIdVenta(Long id);
}
