package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {
    Optional<Compra> findByIdCompra(Long id);
}
