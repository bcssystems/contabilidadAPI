package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {
    Optional<Empleado> findEmpleadoByPersona_Nombre(String personaNombre);
    Optional<Empleado> findByIdEmpleado(Integer idUser);
    Optional<Empleado> findByPersonaIdPerson(Integer personaId);
    Optional<Empleado> findByUsuarioIdUser(Integer usuarioId);
}
