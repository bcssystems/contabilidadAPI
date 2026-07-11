package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Cliente;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.ClienteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteServiceImpl(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public Page<Cliente> buscarConFiltro(String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return clienteRepository.buscarConFiltro(search, pageable);
        }
        return clienteRepository.findByActivoTrue(pageable);
    }

    @Override
    public Cliente findById(Integer id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));
    }

    @Override
    @Transactional
    public Cliente save(Cliente cliente) {
        cliente.setActivo(true);
        cliente.setFechaRegistro(LocalDateTime.now());
        if (cliente.getSaldoActual() == null) cliente.setSaldoActual(0.0);
        if (cliente.getLimiteCredito() == null) cliente.setLimiteCredito(0.0);
        return clienteRepository.save(cliente);
    }

    @Override
    @Transactional
    public Cliente update(Integer id, Cliente cliente) {
        Cliente existing = findById(id);
        if (cliente.getNombre() != null) existing.setNombre(cliente.getNombre());
        if (cliente.getApellidoPaterno() != null) existing.setApellidoPaterno(cliente.getApellidoPaterno());
        if (cliente.getApellidoMaterno() != null) existing.setApellidoMaterno(cliente.getApellidoMaterno());
        if (cliente.getTelefono() != null) existing.setTelefono(cliente.getTelefono());
        if (cliente.getCodigoPais() != null) existing.setCodigoPais(cliente.getCodigoPais());
        if (cliente.getEmpresa() != null) existing.setEmpresa(cliente.getEmpresa());
        if (cliente.getRegimenFiscal() != null) existing.setRegimenFiscal(cliente.getRegimenFiscal());
        if (cliente.getCp() != null) existing.setCp(cliente.getCp());
        if (cliente.getDireccion() != null) existing.setDireccion(cliente.getDireccion());
        if (cliente.getTieneCredito() != null) existing.setTieneCredito(cliente.getTieneCredito());
        if (cliente.getLimiteCredito() != null) existing.setLimiteCredito(cliente.getLimiteCredito());
        if (cliente.getSaldoActual() != null) existing.setSaldoActual(cliente.getSaldoActual());
        return clienteRepository.save(existing);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        Cliente cliente = findById(id);
        cliente.setActivo(false);
        clienteRepository.save(cliente);
    }

    @Override
    public List<Cliente> findAllActivos() {
        return clienteRepository.findByActivoTrue();
    }
}
