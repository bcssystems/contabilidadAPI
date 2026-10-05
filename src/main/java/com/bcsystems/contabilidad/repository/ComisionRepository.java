package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.Comision;
import com.bcsystems.contabilidad.domain.en.EstadoComision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComisionRepository extends JpaRepository<Comision, Long> {
    List<Comision> findByVentaIdVenta(Long ventaId);
    List<Comision> findByUsuarioIdUser(Integer usuarioId);
    List<Comision> findByUsuarioIdUserAndEstadoComision(Integer usuarioId, EstadoComision estado);
    List<Comision> findByEstadoComision(EstadoComision estado);
    List<Comision> findByNominaIdNomina(Long nominaId);
}
