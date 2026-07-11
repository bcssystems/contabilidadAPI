package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.ProductoMultimedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoMultimediaRepository extends JpaRepository<ProductoMultimedia, Integer> {
    List<ProductoMultimedia> findByProductoIdProductoOrderByEsPrincipalDesc(Integer idProducto);
    void deleteByProductoIdProducto(Integer idProducto);
}
