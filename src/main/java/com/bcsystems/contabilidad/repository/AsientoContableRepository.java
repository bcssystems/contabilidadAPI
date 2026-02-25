package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.AsientoContable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AsientoContableRepository extends JpaRepository<AsientoContable, Long> {
    Optional<AsientoContable> findByReferencia(String referencia);
}
