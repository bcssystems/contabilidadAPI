package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.CostoPromedio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CostoPromedioRepository extends JpaRepository<CostoPromedio, Long> {

    Optional<CostoPromedio> findByProductoPadreId(Long productoPadreId);
}
