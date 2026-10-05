package com.bcsystems.contabilidad.repository;
import com.bcsystems.contabilidad.domain.EmpleadoHorario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface EmpleadoHorarioRepository extends JpaRepository<EmpleadoHorario, Integer> {
    Optional<EmpleadoHorario> findFirstByEmpleadoIdEmpleadoAndActivoTrueOrderByIdDesc(Integer empleadoId);
}
