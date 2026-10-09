package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.PrestamoParcialidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrestamoParcialidadRepository extends JpaRepository<PrestamoParcialidad, Long> {
    List<PrestamoParcialidad> findByPrestamoIdPrestamoOrderByNumero(Long prestamoId);
}
