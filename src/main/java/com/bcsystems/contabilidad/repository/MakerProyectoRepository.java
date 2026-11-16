package com.bcsystems.contabilidad.repository;
import com.bcsystems.contabilidad.domain.MakerProyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
public interface MakerProyectoRepository extends JpaRepository<MakerProyecto, Integer> {
    Page<MakerProyecto> findAllByOrderByFechaCreacionDesc(Pageable pageable);
    java.util.List<MakerProyecto> findByCotizacionId(Integer cotizacionId);
}
