package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.Variante;
import com.bcsystems.contabilidad.domain.en.StatusProducto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VarianteRepository extends JpaRepository<Variante, Long> {

    Page<Variante> findByProductoPadreIdAndActivoTrue(Long productoPadreId, Pageable pageable);

    List<Variante> findByProductoPadreIdAndActivoTrue(Long productoPadreId);

    Page<Variante> findByActivoTrueAndStatusOrderByFechaEntradaAsc(StatusProducto status, Pageable pageable);

    List<Variante> findByActivoTrueAndStatus(StatusProducto status);

    long countByActivoTrueAndStatus(StatusProducto status);

    Optional<Variante> findByNumeroSerie(String numeroSerie);

    boolean existsByNumeroSerie(String numeroSerie);

    long countByProductoPadreIdAndActivoTrueAndStatus(Long productoPadreId, StatusProducto status);
}
