package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.AsientoContable;
import com.bcsystems.contabilidad.domain.Compra;
import com.bcsystems.contabilidad.domain.Proveedor;
import com.bcsystems.contabilidad.dto.CompraRegisterRequest;
import com.bcsystems.contabilidad.dto.CompraUpdateRequest;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.AsientoContableRepository;
import com.bcsystems.contabilidad.repository.CompraRepository;
import com.bcsystems.contabilidad.repository.ProveedorRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CompraService {

    private final CompraRepository compraRepository;
    private final ProveedorRepository proveedorRepository;
    private final AsientoContableRepository asientoContableRepository;

    public CompraService(CompraRepository compraRepository,
                        ProveedorRepository proveedorRepository,
                        AsientoContableRepository asientoContableRepository) {
        this.compraRepository = compraRepository;
        this.proveedorRepository = proveedorRepository;
        this.asientoContableRepository = asientoContableRepository;
    }

    @Transactional
    public Compra save(CompraRegisterRequest request) {
        Compra compra = Compra.builder()
                .fecha_compra(request.fecha_compra())
                .total_compra(request.total_compra())
                .build();

        if (request.proveedorId() != null) {
            Optional<Proveedor> proveedor = proveedorRepository.findById(request.proveedorId());
            if (proveedor.isPresent()) {
                compra.setProveedor(proveedor.get());
            }
        }

        if (request.asientoId() != null) {
            Optional<AsientoContable> asiento = asientoContableRepository.findById(request.asientoId());
            if (asiento.isPresent()) {
                compra.setAsientoContable(asiento.get());
            }
        }

        return compraRepository.save(compra);
    }

    public List<Compra> findAll() {
        return compraRepository.findAll();
    }

    public Optional<Compra> findById(Long id) {
        Optional<Compra> compra = compraRepository.findById(id);
        if (compra.isPresent()) return compra;
        throw new NotFoundException("Compra no encontrada");
    }

    @Transactional
    public Compra update(Long id, CompraUpdateRequest request) {
        Optional<Compra> compraOpt = compraRepository.findById(id);
        if (!compraOpt.isPresent()) {
            throw new NotFoundException("Compra no encontrada");
        }

        Compra compra = compraOpt.get();

        if (request.fecha_compra() != null) {
            compra.setFecha_compra(request.fecha_compra());
        }
        if (request.total_compra() != null) {
            compra.setTotal_compra(request.total_compra());
        }
        if (request.estadoCompra() != null) {
            compra.setEstadoCompra(request.estadoCompra());
        }
        if (request.proveedorId() != null) {
            Optional<Proveedor> proveedor = proveedorRepository.findById(request.proveedorId());
            if (proveedor.isPresent()) {
                compra.setProveedor(proveedor.get());
            }
        }
        if (request.asientoId() != null) {
            Optional<AsientoContable> asiento = asientoContableRepository.findById(request.asientoId());
            if (asiento.isPresent()) {
                compra.setAsientoContable(asiento.get());
            }
        }

        return compraRepository.save(compra);
    }

    @Transactional
    public void delete(Long id) {
        Optional<Compra> compraOpt = compraRepository.findById(id);
        if (!compraOpt.isPresent()) {
            throw new NotFoundException("Compra no encontrada");
        }
        compraRepository.delete(compraOpt.get());
    }
}
