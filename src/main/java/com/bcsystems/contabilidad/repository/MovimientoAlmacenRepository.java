package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.MovimientoAlmacen;
import com.bcsystems.contabilidad.domain.en.EstadoMovimiento;
import com.bcsystems.contabilidad.domain.en.TipoMovimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoAlmacenRepository extends JpaRepository<MovimientoAlmacen, Long> {

    Page<MovimientoAlmacen> findByTipoOrderByFechaSolicitudDesc(TipoMovimiento tipo, Pageable pageable);

    Page<MovimientoAlmacen> findByTipoAndEstadoOrderByFechaSolicitudAsc(TipoMovimiento tipo, EstadoMovimiento estado, Pageable pageable);

    List<MovimientoAlmacen> findByEstado(EstadoMovimiento estado);

    List<MovimientoAlmacen> findByVarianteId(Long varianteId);

    long countByEstado(EstadoMovimiento estado);
}
