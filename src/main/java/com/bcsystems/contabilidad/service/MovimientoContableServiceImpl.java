package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.AsientoContable;
import com.bcsystems.contabilidad.domain.CuentaContable;
import com.bcsystems.contabilidad.domain.MovimientoContable;
import com.bcsystems.contabilidad.domain.en.TipoAsientoContable;
import com.bcsystems.contabilidad.dto.MovimientoContableRegisterRequest;
import com.bcsystems.contabilidad.dto.MovimientoContableUpdateRequest;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.AsientoContableRepository;
import com.bcsystems.contabilidad.repository.CuentaContableRepository;
import com.bcsystems.contabilidad.repository.MovimientoContableRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class MovimientoContableServiceImpl implements MovimientoContableService {

    private final MovimientoContableRepository movimientoContableRepository;
    private final CuentaContableRepository cuentaContableRepository;
    private final AsientoContableRepository asientoContableRepository;

    public MovimientoContableServiceImpl(MovimientoContableRepository movimientoContableRepository,
                                         CuentaContableRepository cuentaContableRepository,
                                         AsientoContableRepository asientoContableRepository) {
        this.movimientoContableRepository = movimientoContableRepository;
        this.cuentaContableRepository = cuentaContableRepository;
        this.asientoContableRepository = asientoContableRepository;
    }

    @Transactional
    public MovimientoContable save(MovimientoContableRegisterRequest request) {
        Optional<CuentaContable> cuenta = cuentaContableRepository.findById(request.cuentaContableId());
        if (cuenta.isEmpty()) {
            throw new NotFoundException("Cuenta contable no encontrada");
        }

        Optional<AsientoContable> asiento = asientoContableRepository.findById(request.asientoContableId());
        if (asiento.isEmpty()) {
            throw new NotFoundException("Asiento contable no encontrado");
        }

        if (asiento.get().getTipoAsientoContable() == TipoAsientoContable.CONFIRMADO) {
            throw new IllegalStateException("No se puede modificar un asiento confirmado");
        }

        MovimientoContable movimiento = MovimientoContable.builder()
                .debe(request.debe() != null ? request.debe() : BigDecimal.ZERO)
                .haber(request.haber() != null ? request.haber() : BigDecimal.ZERO)
                .cuentaContable(cuenta.get())
                .asientoContable(asiento.get())
                .build();

        return movimientoContableRepository.save(movimiento);
    }

    @Override
    public List<MovimientoContable> findAll() {
        return movimientoContableRepository.findAll();
    }

    @Override
    public Optional<MovimientoContable> findById(Long id) {
        Optional<MovimientoContable> movimiento = movimientoContableRepository.findById(id);
        if (movimiento.isPresent()) return movimiento;
        throw new NotFoundException("Movimiento contable no encontrado");
    }

    @Override
    @Transactional
    public MovimientoContable update(Long id, MovimientoContableUpdateRequest request) {
        Optional<MovimientoContable> movimientoOpt = movimientoContableRepository.findById(id);
        if (!movimientoOpt.isPresent()) {
            throw new NotFoundException("Movimiento contable no encontrado");
        }

        MovimientoContable movimiento = movimientoOpt.get();

        if (movimiento.getAsientoContable().getTipoAsientoContable() == TipoAsientoContable.CONFIRMADO) {
            throw new IllegalStateException("No se puede modificar un asiento confirmado");
        }

        if (request.cuentaContableId() != null) {
            Optional<CuentaContable> cuenta = cuentaContableRepository.findById(request.cuentaContableId());
            if (cuenta.isEmpty()) {
                throw new NotFoundException("Cuenta contable no encontrada");
            }
            movimiento.setCuentaContable(cuenta.get());
        }

        if (request.debe() != null) {
            movimiento.setDebe(request.debe());
        }
        if (request.haber() != null) {
            movimiento.setHaber(request.haber());
        }

        return movimientoContableRepository.save(movimiento);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Optional<MovimientoContable> movimientoOpt = movimientoContableRepository.findById(id);
        if (!movimientoOpt.isPresent()) {
            throw new NotFoundException("Movimiento contable no encontrado");
        }

        MovimientoContable movimiento = movimientoOpt.get();

        if (movimiento.getAsientoContable().getTipoAsientoContable() == TipoAsientoContable.CONFIRMADO) {
            throw new IllegalStateException("No se puede eliminar un movimiento de asiento confirmado");
        }

        movimientoContableRepository.delete(movimiento);
    }

    @Override
    public List<MovimientoContable> findByAsiento(Long asientoId) {
        return movimientoContableRepository.findByAsientoContableIdAsientoContable(asientoId);
    }

    @Override
    public List<MovimientoContable> findByCuenta(Long cuentaId) {
        return movimientoContableRepository.findByCuentaContableIdCuentaContable(cuentaId);
    }
}
