package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.MovimientoStock;
import com.bcsystems.contabilidad.domain.en.TipoMovimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Integer> {

    @Query("SELECT m FROM MovimientoStock m WHERE " +
           "(:idProducto IS NULL OR m.producto.idProducto = :idProducto) AND " +
           "(:idSucursal IS NULL OR m.sucursal.idSucursal = :idSucursal) AND " +
           "(:tipo IS NULL OR m.tipoMovimiento = :tipo) AND " +
           "(:fechaInicio IS NULL OR m.fechaMovimiento >= :fechaInicio) AND " +
           "(:fechaFin IS NULL OR m.fechaMovimiento <= :fechaFin) " +
           "ORDER BY m.fechaMovimiento DESC")
    Page<MovimientoStock> buscarConFiltros(@Param("idProducto") Integer idProducto,
                                           @Param("idSucursal") Integer idSucursal,
                                           @Param("tipo") TipoMovimiento tipo,
                                           @Param("fechaInicio") LocalDateTime fechaInicio,
                                           @Param("fechaFin") LocalDateTime fechaFin,
                                           Pageable pageable);

    Page<MovimientoStock> findByProductoIdProductoOrderByFechaMovimientoDesc(Integer idProducto, Pageable pageable);

    @Query("SELECT m FROM MovimientoStock m WHERE " +
           "(:fechaInicio IS NULL OR m.fechaMovimiento >= :fechaInicio) AND " +
           "(:fechaFin IS NULL OR m.fechaMovimiento <= :fechaFin) AND " +
           "(:tipo IS NULL OR m.tipoMovimiento = :tipo) " +
           "ORDER BY m.fechaMovimiento DESC")
    Page<MovimientoStock> findAllFiltered(@Param("fechaInicio") LocalDateTime fechaInicio,
                                          @Param("fechaFin") LocalDateTime fechaFin,
                                          @Param("tipo") TipoMovimiento tipo,
                                          Pageable pageable);
}
