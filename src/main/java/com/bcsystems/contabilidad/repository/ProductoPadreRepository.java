package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.ProductoPadre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoPadreRepository extends JpaRepository<ProductoPadre, Long> {

    Page<ProductoPadre> findByActivoTrue(Pageable pageable);

    List<ProductoPadre> findByActivoTrue();

    Optional<ProductoPadre> findBySku(String sku);

    boolean existsBySku(String sku);
}
