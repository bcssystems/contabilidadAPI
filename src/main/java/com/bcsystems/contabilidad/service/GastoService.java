package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.AsientoContable;
import com.bcsystems.contabilidad.domain.CuentaContable;
import com.bcsystems.contabilidad.domain.Gasto;
import com.bcsystems.contabilidad.dto.GastoRegisterRequest;
import com.bcsystems.contabilidad.dto.GastoResponse;
import com.bcsystems.contabilidad.dto.GastoUpdateRequest;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.AsientoContableRepository;
import com.bcsystems.contabilidad.repository.CuentaContableRepository;
import com.bcsystems.contabilidad.repository.GastoRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GastoService {

    private final GastoRepository gastoRepository;
    private final CuentaContableRepository cuentaContableRepository;
    private final AsientoContableRepository asientoContableRepository;

    public GastoService(GastoRepository gastoRepository,
                       CuentaContableRepository cuentaContableRepository,
                       AsientoContableRepository asientoContableRepository) {
        this.gastoRepository = gastoRepository;
        this.cuentaContableRepository = cuentaContableRepository;
        this.asientoContableRepository = asientoContableRepository;
    }

    @Transactional
    public Gasto save(GastoRegisterRequest request) {
        Gasto gasto = Gasto.builder()
                .fecha_gasto(request.fecha_gasto())
                .concepto(request.concepto())
                .monto(request.monto())
                .build();

        if (request.cuentaContableId() != null) {
            Optional<CuentaContable> cuenta = cuentaContableRepository.findById(request.cuentaContableId());
            if (cuenta.isPresent()) {
                gasto.setCuentaContable(cuenta.get());
            }
        }

        if (request.asientoId() != null) {
            Optional<AsientoContable> asiento = asientoContableRepository.findById(request.asientoId());
            if (asiento.isPresent()) {
                gasto.setAsientoContable(asiento.get());
            }
        }

        return gastoRepository.save(gasto);
    }

    public List<Gasto> findAll() {
        return gastoRepository.findAll();
    }

    public Page<GastoResponse> findAllPageable(PageRequest pageRequest) {
        Page<Gasto> gastos = gastoRepository.findAll(pageRequest);
        return gastos.map(this::toResponse);
    }

    private GastoResponse toResponse(Gasto gasto) {
        return new GastoResponse(
                gasto.getIdGasto(),
                gasto.getFecha_gasto(),
                gasto.getConcepto(),
                gasto.getMonto(),
                gasto.getCuentaContable() != null ? gasto.getCuentaContable().getIdCuentaContable() : null,
                gasto.getAsientoContable() != null ? gasto.getAsientoContable().getIdAsientoContable() : null
        );
    }

    public Optional<Gasto> findById(Long id) {
        Optional<Gasto> gasto = gastoRepository.findById(id);
        if (gasto.isPresent()) return gasto;
        throw new NotFoundException("Gasto no encontrado");
    }

    @Transactional
    public Gasto update(Long id, GastoUpdateRequest request) {
        Optional<Gasto> gastoOpt = gastoRepository.findById(id);
        if (!gastoOpt.isPresent()) {
            throw new NotFoundException("Gasto no encontrado");
        }

        Gasto gasto = gastoOpt.get();

        if (request.fecha_gasto() != null) {
            gasto.setFecha_gasto(request.fecha_gasto());
        }
        if (request.concepto() != null && !request.concepto().trim().isEmpty()) {
            gasto.setConcepto(request.concepto().trim());
        }
        if (request.monto() != null) {
            gasto.setMonto(request.monto());
        }
        if (request.cuentaContableId() != null) {
            Optional<CuentaContable> cuenta = cuentaContableRepository.findById(request.cuentaContableId());
            if (cuenta.isPresent()) {
                gasto.setCuentaContable(cuenta.get());
            }
        }
        if (request.asientoId() != null) {
            Optional<AsientoContable> asiento = asientoContableRepository.findById(request.asientoId());
            if (asiento.isPresent()) {
                gasto.setAsientoContable(asiento.get());
            }
        }

        return gastoRepository.save(gasto);
    }

    @Transactional
    public void delete(Long id) {
        Optional<Gasto> gastoOpt = gastoRepository.findById(id);
        if (!gastoOpt.isPresent()) {
            throw new NotFoundException("Gasto no encontrado");
        }
        gastoRepository.delete(gastoOpt.get());
    }
}
