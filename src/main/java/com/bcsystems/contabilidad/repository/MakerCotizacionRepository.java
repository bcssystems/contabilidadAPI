package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.MakerCotizacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MakerCotizacionRepository extends JpaRepository<MakerCotizacion, Integer> {
    Page<MakerCotizacion> findAllByOrderByFechaDesc(Pageable pageable);
    Optional<MakerCotizacion> findByFolio(String folio);
}
