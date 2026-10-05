package com.bcsystems.contabilidad.repository;
import com.bcsystems.contabilidad.domain.EvidenciaAsistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import com.bcsystems.contabilidad.domain.en.EstadoEvidencia;
import java.util.List;
public interface EvidenciaAsistenciaRepository extends JpaRepository<EvidenciaAsistencia, Integer> {
    List<EvidenciaAsistencia> findByEstadoOrderByCreadoEnDesc(EstadoEvidencia estado);
}
