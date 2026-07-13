package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    Optional<Producto> findBySku(String sku);

    @Query("SELECT p FROM Producto p WHERE " +
           "(:search IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:activo IS NULL OR p.activo = :activo) AND " +
           "(:tieneVariantes IS NULL OR p.tieneVariantes = :tieneVariantes) " +
           "ORDER BY p.nombre ASC")
    Page<Producto> buscarConFiltros(@Param("search") String search,
                                    @Param("activo") Boolean activo,
                                    @Param("tieneVariantes") Boolean tieneVariantes,
                                    Pageable pageable);

    Page<Producto> findByActivoTrue(Pageable pageable);

    @Query("SELECT p FROM Producto p WHERE p.activo = true AND " +
           "(:idSucursal IS NULL OR EXISTS (SELECT i FROM InventarioSucursal i WHERE i.producto.idProducto = p.idProducto AND i.sucursal.idSucursal = :idSucursal))")
    Page<Producto> findByActivoAndSucursal(@Param("idSucursal") Integer idSucursal, Pageable pageable);

    List<Producto> findByActivoTrue();

    @Query("SELECT DISTINCT p FROM Producto p LEFT JOIN FETCH p.productoPadre LEFT JOIN FETCH p.multimedia WHERE p.activo = true")
    List<Producto> findAllEcommerce();
    List<Producto> findByProductoPadreIdProducto(Integer idProductoPadre);
    List<Producto> findByTieneVariantesTrueAndActivoTrue();
    long countByActivoTrue();
}
