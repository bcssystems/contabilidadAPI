package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    @Query("SELECT c FROM Cliente c WHERE " +
           "(:search IS NULL OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.telefono) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY c.nombre ASC")
    Page<Cliente> buscarConFiltro(@Param("search") String search, Pageable pageable);

    Page<Cliente> findByActivoTrue(Pageable pageable);
    Page<Cliente> findByActivoTrueAndTieneCreditoTrue(Pageable pageable);
    java.util.List<Cliente> findByActivoTrue();
}
