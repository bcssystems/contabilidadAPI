package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.Deduccion;
import com.bcsystems.contabilidad.domain.en.EstadoDeduccion;
import com.bcsystems.contabilidad.domain.en.TipoDeduccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeduccionRepository extends JpaRepository<Deduccion, Long> {
    List<Deduccion> findByUsuarioIdUser(Integer usuarioId);
    List<Deduccion> findByUsuarioIdUserAndEstado(Integer usuarioId, EstadoDeduccion estado);
    List<Deduccion> findByUsuarioIdUserAndEstadoIn(Integer usuarioId, List<EstadoDeduccion> estados);
    List<Deduccion> findByEstado(EstadoDeduccion estado);
    List<Deduccion> findByNominaIdNomina(Long nominaId);
    boolean existsByAsistenciaIdAndTipo(Integer asistenciaId, TipoDeduccion tipo);
    boolean existsByUsuarioIdUserAndFechaAndTipo(Integer usuarioId, java.time.LocalDate fecha, TipoDeduccion tipo);
    List<Deduccion> findByUsuarioIdUserAndFechaAndTipoAndEstadoIn(Integer usuarioId, java.time.LocalDate fecha,
                                                                   TipoDeduccion tipo, List<EstadoDeduccion> estados);
    boolean existsByPrestamoParcialidadIdParcialidad(Long parcialidadId);
    List<Deduccion> findByPrestamoParcialidadIdParcialidad(Long parcialidadId);
}
