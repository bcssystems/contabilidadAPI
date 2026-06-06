package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.Producto;
import com.bcsystems.contabilidad.domain.en.CategoriaProducto;
import com.bcsystems.contabilidad.domain.en.EstadoProducto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Page<Producto> findByActivoTrue(Pageable pageable);

    Page<Producto> findByActivoTrueAndCategoria(Pageable pageable, CategoriaProducto categoria);

    Page<Producto> findByActivoTrueAndEstado(Pageable pageable, EstadoProducto estado);

    Page<Producto> findByActivoTrueAndCategoriaAndEstado(Pageable pageable, CategoriaProducto categoria, EstadoProducto estado);

    List<Producto> findByActivoTrue();

    long countByActivoTrue();
}
