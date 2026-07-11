package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;

public interface PedidoService {
    Page<Pedido> buscarConFiltros(String search, String estado, Long idProveedor, Pageable pageable);
    Pedido findById(Integer id);
    Pedido save(Pedido pedido);
    Pedido recibir(Integer id, List<Map<String, Object>> detalles);
    Pedido cancelar(Integer id);
    Pedido completar(Integer id);
}
