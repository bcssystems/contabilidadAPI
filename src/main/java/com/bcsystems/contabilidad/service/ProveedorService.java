package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.CuentaContable;
import com.bcsystems.contabilidad.domain.Proveedor;
import com.bcsystems.contabilidad.dto.ProveedorRegisterRequest;
import com.bcsystems.contabilidad.dto.ProveedorUpdateRequest;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.CuentaContableRepository;
import com.bcsystems.contabilidad.repository.ProveedorRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;
    private final CuentaContableRepository cuentaContableRepository;

    public ProveedorService(ProveedorRepository proveedorRepository,
                          CuentaContableRepository cuentaContableRepository) {
        this.proveedorRepository = proveedorRepository;
        this.cuentaContableRepository = cuentaContableRepository;
    }

    @Transactional
    public Proveedor save(ProveedorRegisterRequest request) {
        Proveedor proveedor = Proveedor.builder()
                .nombre(request.nombre())
                .build();

        if (request.cuentaPorPagarId() != null) {
            Optional<CuentaContable> cuenta = cuentaContableRepository.findById(request.cuentaPorPagarId());
            if (cuenta.isPresent()) {
                proveedor.setCuentaPorPagar(cuenta.get());
            }
        }

        return proveedorRepository.save(proveedor);
    }

    public List<Proveedor> findAll() {
        return proveedorRepository.findAll();
    }

    public Optional<Proveedor> findById(Long id) {
        Optional<Proveedor> proveedor = proveedorRepository.findById(id);
        if (proveedor.isPresent()) return proveedor;
        throw new NotFoundException("Proveedor no encontrado");
    }

    @Transactional
    public Proveedor update(Long id, ProveedorUpdateRequest request) {
        Optional<Proveedor> proveedorOpt = proveedorRepository.findById(id);
        if (!proveedorOpt.isPresent()) {
            throw new NotFoundException("Proveedor no encontrado");
        }

        Proveedor proveedor = proveedorOpt.get();

        if (request.nombre() != null && !request.nombre().trim().isEmpty()) {
            proveedor.setNombre(request.nombre().trim());
        }
        if (request.cuentaPorPagarId() != null) {
            Optional<CuentaContable> cuenta = cuentaContableRepository.findById(request.cuentaPorPagarId());
            if (cuenta.isPresent()) {
                proveedor.setCuentaPorPagar(cuenta.get());
            }
        }

        return proveedorRepository.save(proveedor);
    }

    @Transactional
    public void delete(Long id) {
        Optional<Proveedor> proveedorOpt = proveedorRepository.findById(id);
        if (!proveedorOpt.isPresent()) {
            throw new NotFoundException("Proveedor no encontrado");
        }
        proveedorRepository.delete(proveedorOpt.get());
    }
}
