package com.bcsystems.contabilidad.repository;
import com.bcsystems.contabilidad.domain.JustificacionAsistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.bcsystems.contabilidad.domain.en.EstadoJustificacion;
public interface JustificacionAsistenciaRepository extends JpaRepository<JustificacionAsistencia, Integer> {
    List<JustificacionAsistencia> findByUsuarioIdUserOrderByCreadoEnDesc(Integer usuarioId);
    List<JustificacionAsistencia> findByEstadoOrderByCreadoEnDesc(EstadoJustificacion estado);
    boolean existsByAsistenciaIdAndEstado(Integer asistenciaId, EstadoJustificacion estado);
}
