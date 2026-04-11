package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.AsientoContable;
import com.bcsystems.contabilidad.domain.Venta;
import com.bcsystems.contabilidad.domain.en.EstadoVenta;
import com.bcsystems.contabilidad.dto.VentaRegisterRequest;
import com.bcsystems.contabilidad.dto.VentaResponse;
import com.bcsystems.contabilidad.dto.VentaUpdateRequest;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.AsientoContableRepository;
import com.bcsystems.contabilidad.repository.VentaRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final AsientoContableRepository asientoContableRepository;

    public VentaServiceImpl(VentaRepository ventaRepository, AsientoContableRepository asientoContableRepository) {
        this.ventaRepository = ventaRepository;
        this.asientoContableRepository = asientoContableRepository;
    }

    @Transactional
    public Venta save(VentaRegisterRequest request) {
        Venta venta = Venta.builder()
                .fecha_venta(request.fecha_venta())
                .tipoVenta(request.tipoVenta())
                .estadoVenta(EstadoVenta.PENDIENTE)
                .total(request.total())
                .costo(request.costo())
                .build();

        if (request.asientoId() != null) {
            Optional<AsientoContable> asiento = asientoContableRepository.findById(request.asientoId());
            if (asiento.isPresent()) {
                venta.setAsiento(asiento.get());
            }
        }

        return ventaRepository.save(venta);
    }

    @Override
    public List<Venta> findAll() {
        return ventaRepository.findAll();
    }

    @Override
    public Page<VentaResponse> findAllPageable(PageRequest pageRequest) {
        Page<Venta> ventas = ventaRepository.findAll(pageRequest);
        return ventas.map(this::toResponse);
    }

    private VentaResponse toResponse(Venta venta) {
        return new VentaResponse(
                venta.getIdVenta(),
                venta.getFecha_venta(),
                venta.getTipoVenta(),
                venta.getEstadoVenta(),
                venta.getTotal(),
                venta.getCosto(),
                venta.getAsiento() != null ? venta.getAsiento().getIdAsientoContable() : null,
                List.of()
        );
    }

    @Override
    public Optional<Venta> findById(Long id) {
        Optional<Venta> venta = ventaRepository.findById(id);
        if (venta.isPresent()) return venta;
        throw new NotFoundException("Venta no encontrada");
    }

    @Override
    @Transactional
    public Venta update(Long id, VentaUpdateRequest request) {
        Optional<Venta> ventaOpt = ventaRepository.findById(id);
        if (!ventaOpt.isPresent()) {
            throw new NotFoundException("Venta no encontrada");
        }

        Venta venta = ventaOpt.get();

        if (request.fecha_venta() != null) {
            venta.setFecha_venta(request.fecha_venta());
        }
        if (request.tipoVenta() != null) {
            venta.setTipoVenta(request.tipoVenta());
        }
        if (request.estadoVenta() != null) {
            venta.setEstadoVenta(request.estadoVenta());
        }
        if (request.total() != null) {
            venta.setTotal(request.total());
        }
        if (request.costo() != null) {
            venta.setCosto(request.costo());
        }
        if (request.asientoId() != null) {
            Optional<AsientoContable> asiento = asientoContableRepository.findById(request.asientoId());
            if (asiento.isPresent()) {
                venta.setAsiento(asiento.get());
            }
        }

        return ventaRepository.save(venta);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Optional<Venta> ventaOpt = ventaRepository.findById(id);
        if (!ventaOpt.isPresent()) {
            throw new NotFoundException("Venta no encontrada");
        }
        ventaRepository.delete(ventaOpt.get());
    }
}
