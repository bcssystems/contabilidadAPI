package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.CuentaContable;
import com.bcsystems.contabilidad.dto.CuentaContableRegisterRequest;
import com.bcsystems.contabilidad.dto.CuentaContableResponse;
import com.bcsystems.contabilidad.dto.CuentaContableUpdateRequest;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.CuentaContableRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CuentaContableServiceImpl implements CuentaContableService {

    private final CuentaContableRepository cuentaContableRepository;

    public CuentaContableServiceImpl(CuentaContableRepository cuentaContableRepository) {
        this.cuentaContableRepository = cuentaContableRepository;
    }

    @Transactional
    public CuentaContable save(CuentaContableRegisterRequest request) {
        CuentaContable cuenta = CuentaContable.builder()
                .codigo(request.codigo())
                .nombre(request.nombre())
                .tipoCuentaContable(request.tipoCuentaContable())
                .activa(true)
                .build();

        if (request.cuentaPadreId() != null) {
            Optional<CuentaContable> padre = cuentaContableRepository.findById(request.cuentaPadreId());
            if (padre.isPresent()) {
                cuenta.setCuentaPadre(padre.get());
            }
        }

        return cuentaContableRepository.save(cuenta);
    }

    @Override
    public List<CuentaContable> findAll() {
        return cuentaContableRepository.findByCuentaPadreIsNull();
    }

    @Override
    public Page<CuentaContableResponse> findAllPageable(PageRequest pageRequest) {
        Page<CuentaContable> cuentas = cuentaContableRepository.findAll(pageRequest);
        return cuentas.map(this::toResponse);
    }

    private CuentaContableResponse toResponse(CuentaContable cuenta) {
        return new CuentaContableResponse(
                cuenta.getIdCuentaContable(),
                cuenta.getCodigo(),
                cuenta.getNombre(),
                cuenta.getTipoCuentaContable(),
                cuenta.getActiva(),
                cuenta.getCuentaPadre() != null ? cuenta.getCuentaPadre().getIdCuentaContable() : null,
                cuenta.getSubcuentas() != null 
                    ? cuenta.getSubcuentas().stream().map(this::toResponse).toList() 
                    : List.of()
        );
    }

    @Override
    public Optional<CuentaContable> findById(Long id) {
        Optional<CuentaContable> cuenta = cuentaContableRepository.findById(id);
        if (cuenta.isPresent()) return cuenta;
        throw new NotFoundException("Cuenta contable no encontrada");
    }

    @Override
    @Transactional
    public CuentaContable update(Long id, CuentaContableUpdateRequest request) {
        Optional<CuentaContable> cuentaOpt = cuentaContableRepository.findById(id);
        if (!cuentaOpt.isPresent()) {
            throw new NotFoundException("Cuenta contable no encontrada");
        }

        CuentaContable cuenta = cuentaOpt.get();

        if (request.codigo() != null && !request.codigo().trim().isEmpty()) {
            cuenta.setCodigo(request.codigo().trim());
        }
        if (request.nombre() != null && !request.nombre().trim().isEmpty()) {
            cuenta.setNombre(request.nombre().trim());
        }
        if (request.tipoCuentaContable() != null) {
            cuenta.setTipoCuentaContable(request.tipoCuentaContable());
        }
        if (request.activa() != null) {
            cuenta.setActiva(request.activa());
        }
        if (request.cuentaPadreId() != null) {
            Optional<CuentaContable> padre = cuentaContableRepository.findById(request.cuentaPadreId());
            if (padre.isPresent()) {
                cuenta.setCuentaPadre(padre.get());
            }
        }

        return cuentaContableRepository.save(cuenta);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Optional<CuentaContable> cuentaOpt = cuentaContableRepository.findById(id);
        if (!cuentaOpt.isPresent()) {
            throw new NotFoundException("Cuenta contable no encontrada");
        }

        CuentaContable cuenta = cuentaOpt.get();
        cuenta.setActiva(false);
        cuentaContableRepository.save(cuenta);
    }
}
