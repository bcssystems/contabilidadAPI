package com.bcsystems.contabilidad.repository;
import com.bcsystems.contabilidad.domain.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.*;
public interface AsistenciaRepository extends JpaRepository<Asistencia, Integer> {
    Optional<Asistencia> findByUsuarioIdUserAndFecha(Integer usuarioId, LocalDate fecha);
    List<Asistencia> findByUsuarioIdUserOrderByFechaDesc(Integer usuarioId);
}
