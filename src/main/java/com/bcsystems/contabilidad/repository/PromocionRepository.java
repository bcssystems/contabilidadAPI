package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.Promocion;
import com.bcsystems.contabilidad.domain.en.TipoPromocion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PromocionRepository extends JpaRepository<Promocion, Integer> {

    @Query("SELECT p FROM Promocion p WHERE " +
           "(:tipo IS NULL OR p.tipo = :tipo) AND " +
           "(:activo IS NULL OR p.activo = :activo) " +
           "ORDER BY p.fechaCreacion DESC")
    Page<Promocion> buscarConFiltros(@Param("tipo") TipoPromocion tipo,
                                     @Param("activo") Boolean activo,
                                     Pageable pageable);

    Page<Promocion> findByActivoTrue(Pageable pageable);
    java.util.List<Promocion> findByActivoTrue();
}
