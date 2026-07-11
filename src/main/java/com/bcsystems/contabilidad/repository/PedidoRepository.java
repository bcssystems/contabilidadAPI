package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.Pedido;
import com.bcsystems.contabilidad.domain.en.EstadoPedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    @Query("SELECT p FROM Pedido p WHERE " +
           "(:search IS NULL OR LOWER(p.folio) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.proveedor.nombre) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:estado IS NULL OR p.estado = :estado) AND " +
           "(:idProveedor IS NULL OR p.proveedor.idProveedor = :idProveedor) " +
           "ORDER BY p.fechaCreacion DESC")
    Page<Pedido> buscarConFiltros(@Param("search") String search,
                                  @Param("estado") EstadoPedido estado,
                                  @Param("idProveedor") Long idProveedor,
                                  Pageable pageable);
}
