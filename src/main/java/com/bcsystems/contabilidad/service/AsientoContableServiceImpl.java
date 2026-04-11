package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.AsientoContable;
import com.bcsystems.contabilidad.domain.CuentaContable;
import com.bcsystems.contabilidad.domain.MovimientoContable;
import com.bcsystems.contabilidad.domain.en.TipoAsientoContable;
import com.bcsystems.contabilidad.dto.AsientoContableRegisterRequest;
import com.bcsystems.contabilidad.dto.AsientoContableResponse;
import com.bcsystems.contabilidad.dto.AsientoContableUpdateRequest;
import com.bcsystems.contabilidad.dto.MovimientoContableRequest;
import com.bcsystems.contabilidad.dto.MovimientoContableResponse;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.AsientoContableRepository;
import com.bcsystems.contabilidad.repository.CuentaContableRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AsientoContableServiceImpl implements AsientoContableService {

    private final AsientoContableRepository asientoContableRepository;
    private final CuentaContableRepository cuentaContableRepository;

    public AsientoContableServiceImpl(AsientoContableRepository asientoContableRepository,
                                       CuentaContableRepository cuentaContableRepository) {
        this.asientoContableRepository = asientoContableRepository;
        this.cuentaContableRepository = cuentaContableRepository;
    }

    @Transactional
    public AsientoContable save(AsientoContableRegisterRequest request) {
        List<MovimientoContable> movimientos = new ArrayList<>();
        BigDecimal totalDebe = BigDecimal.ZERO;
        BigDecimal totalHaber = BigDecimal.ZERO;

        if (request.movimientos() != null && !request.movimientos().isEmpty()) {
            for (MovimientoContableRequest movReq : request.movimientos()) {
                Optional<CuentaContable> cuenta = cuentaContableRepository.findById(movReq.cuentaContableId());
                if (cuenta.isEmpty()) {
                    throw new NotFoundException("Cuenta contable no encontrada: " + movReq.cuentaContableId());
                }

                MovimientoContable movimiento = MovimientoContable.builder()
                        .debe(movReq.debe() != null ? movReq.debe() : BigDecimal.ZERO)
                        .haber(movReq.haber() != null ? movReq.haber() : BigDecimal.ZERO)
                        .cuentaContable(cuenta.get())
                        .build();

                movimientos.add(movimiento);
                totalDebe = totalDebe.add(movimiento.getDebe());
                totalHaber = totalHaber.add(movimiento.getHaber());
            }
        }

        if (totalDebe.compareTo(totalHaber) != 0) {
            throw new IllegalArgumentException("El asiento no cuadra. Debe: " + totalDebe + ", Haber: " + totalHaber);
        }

        AsientoContable asiento = AsientoContable.builder()
                .fecha(request.fecha())
                .referencia(request.referencia())
                .descripcion(request.descripcion())
                .tipoAsientoContable(request.tipoAsientoContable() != null ? request.tipoAsientoContable() : TipoAsientoContable.BORRADOR)
                .movimientos(new ArrayList<>())
                .build();

        AsientoContable savedAsiento = asientoContableRepository.save(asiento);

        for (MovimientoContable mov : movimientos) {
            mov.setAsientoContable(savedAsiento);
        }
        savedAsiento.setMovimientos(movimientos);

        return asientoContableRepository.save(savedAsiento);
    }

    @Override
    public List<AsientoContable> findAll() {
        return asientoContableRepository.findAll();
    }

    @Override
    public Page<AsientoContableResponse> findAllPageable(PageRequest pageRequest) {
        Page<AsientoContable> asientos = asientoContableRepository.findAll(pageRequest);
        return asientos.map(this::toResponse);
    }

    private AsientoContableResponse toResponse(AsientoContable asiento) {
        List<MovimientoContableResponse> movimientos = new ArrayList<>();
        if (asiento.getMovimientos() != null) {
            movimientos = asiento.getMovimientos().stream()
                    .map(mov -> new MovimientoContableResponse(
                            mov.getIdMovimientoContable(),
                            mov.getDebe(),
                            mov.getHaber(),
                            mov.getCuentaContable() != null ? mov.getCuentaContable().getIdCuentaContable() : null,
                            mov.getCuentaContable() != null ? mov.getCuentaContable().getNombre() : null,
                            mov.getCuentaContable() != null ? mov.getCuentaContable().getCodigo() : null,
                            mov.getAsientoContable() != null ? mov.getAsientoContable().getIdAsientoContable() : null
                    ))
                    .toList();
        }
        
        return new AsientoContableResponse(
                asiento.getIdAsientoContable(),
                asiento.getFecha(),
                asiento.getReferencia(),
                asiento.getDescripcion(),
                asiento.getTipoAsientoContable(),
                movimientos
        );
    }

    @Override
    public Optional<AsientoContable> findById(Long id) {
        Optional<AsientoContable> asiento = asientoContableRepository.findById(id);
        if (asiento.isPresent()) return asiento;
        throw new NotFoundException("Asiento contable no encontrado");
    }

    @Override
    @Transactional
    public AsientoContable update(Long id, AsientoContableUpdateRequest request) {
        Optional<AsientoContable> asientoOpt = asientoContableRepository.findById(id);
        if (!asientoOpt.isPresent()) {
            throw new NotFoundException("Asiento contable no encontrado");
        }

        AsientoContable asiento = asientoOpt.get();

        if (asiento.getTipoAsientoContable() == TipoAsientoContable.CONFIRMADO) {
            throw new IllegalStateException("No se puede modificar un asiento confirmado");
        }

        if (request.fecha() != null) {
            asiento.setFecha(request.fecha());
        }
        if (request.referencia() != null && !request.referencia().trim().isEmpty()) {
            asiento.setReferencia(request.referencia().trim());
        }
        if (request.descripcion() != null && !request.descripcion().trim().isEmpty()) {
            asiento.setDescripcion(request.descripcion().trim());
        }
        if (request.tipoAsientoContable() != null) {
            asiento.setTipoAsientoContable(request.tipoAsientoContable());
        }

        if (request.movimientos() != null && !request.movimientos().isEmpty()) {
            List<MovimientoContable> movimientos = new ArrayList<>();
            BigDecimal totalDebe = BigDecimal.ZERO;
            BigDecimal totalHaber = BigDecimal.ZERO;

            for (MovimientoContableRequest movReq : request.movimientos()) {
                Optional<CuentaContable> cuenta = cuentaContableRepository.findById(movReq.cuentaContableId());
                if (cuenta.isEmpty()) {
                    throw new NotFoundException("Cuenta contable no encontrada: " + movReq.cuentaContableId());
                }

                MovimientoContable movimiento = MovimientoContable.builder()
                        .debe(movReq.debe() != null ? movReq.debe() : BigDecimal.ZERO)
                        .haber(movReq.haber() != null ? movReq.haber() : BigDecimal.ZERO)
                        .cuentaContable(cuenta.get())
                        .asientoContable(asiento)
                        .build();

                movimientos.add(movimiento);
                totalDebe = totalDebe.add(movimiento.getDebe());
                totalHaber = totalHaber.add(movimiento.getHaber());
            }

            if (totalDebe.compareTo(totalHaber) != 0) {
                throw new IllegalArgumentException("El asiento no cuadra. Debe: " + totalDebe + ", Haber: " + totalHaber);
            }

            asiento.getMovimientos().clear();
            asiento.getMovimientos().addAll(movimientos);
        }

        return asientoContableRepository.save(asiento);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Optional<AsientoContable> asientoOpt = asientoContableRepository.findById(id);
        if (!asientoOpt.isPresent()) {
            throw new NotFoundException("Asiento contable no encontrado");
        }

        AsientoContable asiento = asientoOpt.get();
        if (asiento.getTipoAsientoContable() == TipoAsientoContable.CONFIRMADO) {
            throw new IllegalStateException("No se puede eliminar un asiento confirmado");
        }

        asientoContableRepository.delete(asiento);
    }
}
