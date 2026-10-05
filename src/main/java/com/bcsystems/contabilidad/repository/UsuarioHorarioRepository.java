package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.UsuarioHorario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioHorarioRepository extends JpaRepository<UsuarioHorario, Integer> {
    Optional<UsuarioHorario> findFirstByUsuarioIdUserAndActivoTrueOrderByIdDesc(Integer usuarioId);
}
