package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.Deduccion;
import com.bcsystems.contabilidad.domain.en.EstadoDeduccion;
import com.bcsystems.contabilidad.domain.en.TipoDeduccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeduccionRepository extends JpaRepository<Deduccion, Long> {
    List<Deduccion> findByEmpleadoIdEmpleado(Integer empleadoId);
    List<Deduccion> findByEmpleadoIdEmpleadoAndEstado(Integer empleadoId, EstadoDeduccion estado);
    List<Deduccion> findByEstado(EstadoDeduccion estado);
    List<Deduccion> findByNominaIdNomina(Long nominaId);
    boolean existsByAsistenciaIdAndTipo(Integer asistenciaId, TipoDeduccion tipo);
}
