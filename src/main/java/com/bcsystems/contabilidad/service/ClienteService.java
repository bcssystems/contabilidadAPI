package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ClienteService {
    Page<Cliente> buscarConFiltro(String search, Pageable pageable);
    Cliente findById(Integer id);
    Cliente save(Cliente cliente);
    Cliente update(Integer id, Cliente cliente);
    void delete(Integer id);
    List<Cliente> findAllActivos();
}
