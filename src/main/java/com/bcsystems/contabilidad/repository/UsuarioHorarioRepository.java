package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.UsuarioHorario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface UsuarioHorarioRepository extends JpaRepository<UsuarioHorario, Integer> {
    Optional<UsuarioHorario> findFirstByUsuarioIdUserAndActivoTrueOrderByIdDesc(Integer usuarioId);
    List<UsuarioHorario> findByHorarioId(Integer horarioId);
}
