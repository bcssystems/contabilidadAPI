package com.bcsystems.contabilidad.repository;
import com.bcsystems.contabilidad.domain.MakerTarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
public interface MakerTareaRepository extends JpaRepository<MakerTarea, Integer> {
    Page<MakerTarea> findAllByOrderByFechaCreacionDesc(Pageable pageable);
    long countByProyectoId(Integer proyectoId);
    long countByProyectoIdAndEstado(Integer proyectoId, String estado);
    java.util.List<MakerTarea> findByProyectoIdOrderByFechaCreacionDesc(Integer proyectoId);
}
