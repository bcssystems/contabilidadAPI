package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.MovimientoContable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovimientoContableRepository extends JpaRepository<MovimientoContable, Long> {
    List<MovimientoContable> findByAsientoContableIdAsientoContable(Long asientoId);
    List<MovimientoContable> findByCuentaContableIdCuentaContable(Long cuentaId);
}
