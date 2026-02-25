package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.Gasto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GastoRepository extends JpaRepository<Gasto, Long> {
    Optional<Gasto> findByIdGasto(Long id);
}
