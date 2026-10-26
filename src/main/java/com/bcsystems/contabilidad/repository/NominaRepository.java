package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.Nomina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface NominaRepository extends JpaRepository<Nomina, Long> {
    Optional<Nomina> findByIdNomina(Long id);
    Optional<Nomina> findFirstByUsuarioIdUserAndFechaInicioAndFechaFinAndEstadoIn(Integer usuarioId,
                                                                                   java.time.LocalDate fechaInicio,
                                                                                   java.time.LocalDate fechaFin,
                                                                                   List<com.bcsystems.contabilidad.domain.en.EstadoNomina> estados);
}
