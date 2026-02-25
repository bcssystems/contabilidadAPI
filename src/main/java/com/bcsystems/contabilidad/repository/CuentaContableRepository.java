package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.CuentaContable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CuentaContableRepository extends JpaRepository<CuentaContable,Long> {
    Optional<CuentaContable> findByCodigo(String codigo);
    Optional<CuentaContable> findByNombre(String nombre);
}
