package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.PedidoDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoDetalleRepository extends JpaRepository<PedidoDetalle, Integer> {
    List<PedidoDetalle> findByPedidoIdPedido(Integer idPedido);
}
