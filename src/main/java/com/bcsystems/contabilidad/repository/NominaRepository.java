package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.Nomina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NominaRepository extends JpaRepository<Nomina, Long> {
    Optional<Nomina> findByIdNomina(Long id);
}
