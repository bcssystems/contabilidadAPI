package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.Comision;
import com.bcsystems.contabilidad.domain.en.EstadoComision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComisionRepository extends JpaRepository<Comision, Long> {
    List<Comision> findByVentaIdVenta(Long ventaId);
    List<Comision> findByEmpleadoIdEmpleado(Long empleadoId);
    List<Comision> findByEmpleadoIdEmpleadoAndEstadoComision(Long empleadoId, EstadoComision estado);
    List<Comision> findByEstadoComision(EstadoComision estado);
}
