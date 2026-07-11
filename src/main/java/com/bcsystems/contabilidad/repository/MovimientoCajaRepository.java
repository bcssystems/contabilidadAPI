package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.MovimientoCaja;
import com.bcsystems.contabilidad.domain.en.TipoMovimientoCaja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimientoCajaRepository extends JpaRepository<MovimientoCaja, Integer> {
    List<MovimientoCaja> findByCajaIdCajaOrderByFechaDesc(Integer idCaja);
    List<MovimientoCaja> findByCajaIdCajaAndTipoAndFechaBetweenOrderByFechaAsc(
        Integer idCaja, TipoMovimientoCaja tipo, LocalDateTime inicio, LocalDateTime fin);
    @Query("SELECT SUM(mc.monto) FROM MovimientoCaja mc WHERE mc.caja.idCaja = :idCaja AND mc.tipo = :tipo AND mc.fecha BETWEEN :inicio AND :fin")
    Double sumMontoByCajaIdCajaAndTipoAndFechaBetween(
            @Param("idCaja") Integer idCaja,
            @Param("tipo") TipoMovimientoCaja tipo,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);
}
