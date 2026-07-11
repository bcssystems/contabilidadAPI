package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.PromocionDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromocionDetalleRepository extends JpaRepository<PromocionDetalle, Integer> {
    List<PromocionDetalle> findByPromocionIdPromocion(Integer idPromocion);
}
