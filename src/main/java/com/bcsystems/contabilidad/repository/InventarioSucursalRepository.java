package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.InventarioSucursal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventarioSucursalRepository extends JpaRepository<InventarioSucursal, Integer> {

    Optional<InventarioSucursal> findByProductoIdProductoAndSucursalIdSucursal(Integer idProducto, Integer sucursalId);

    Optional<InventarioSucursal> findByVarianteIdAndSucursalIdSucursal(Long idVariante, Integer sucursalId);

    List<InventarioSucursal> findByProductoIdProducto(Integer idProducto);

    List<InventarioSucursal> findBySucursalIdSucursal(Integer idSucursal);

    void deleteByProductoIdProducto(Integer idProducto);
}
