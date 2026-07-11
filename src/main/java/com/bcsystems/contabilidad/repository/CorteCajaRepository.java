package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.CorteCaja;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CorteCajaRepository extends JpaRepository<CorteCaja, Integer> {
    List<CorteCaja> findByCajaIdCajaOrderByFechaCierreDesc(Integer idCaja);

    @Query("SELECT c FROM CorteCaja c WHERE " +
           "(:idSucursal IS NULL OR c.caja.sucursal.idSucursal = :idSucursal) AND " +
           "(:idCaja IS NULL OR c.caja.idCaja = :idCaja) AND " +
           "(:fechaInicio IS NULL OR c.fechaCierre >= :fechaInicio) AND " +
           "(:fechaFin IS NULL OR c.fechaCierre <= :fechaFin) " +
           "ORDER BY c.fechaCierre DESC")
    Page<CorteCaja> buscarConFiltros(@Param("idSucursal") Integer idSucursal,
                                     @Param("idCaja") Integer idCaja,
                                     @Param("fechaInicio") LocalDateTime fechaInicio,
                                     @Param("fechaFin") LocalDateTime fechaFin,
                                     Pageable pageable);
}
