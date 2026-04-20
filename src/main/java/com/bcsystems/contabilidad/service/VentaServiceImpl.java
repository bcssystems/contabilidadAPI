package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.AsientoContable;
import com.bcsystems.contabilidad.domain.CuentaContable;
import com.bcsystems.contabilidad.domain.MovimientoContable;
import com.bcsystems.contabilidad.domain.Venta;
import com.bcsystems.contabilidad.domain.en.EstadoVenta;
import com.bcsystems.contabilidad.domain.en.TipoAsientoContable;
import com.bcsystems.contabilidad.domain.en.TipoVenta;
import com.bcsystems.contabilidad.dto.VentaRegisterRequest;
import com.bcsystems.contabilidad.dto.VentaResponse;
import com.bcsystems.contabilidad.dto.VentaUpdateRequest;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.AsientoContableRepository;
import com.bcsystems.contabilidad.repository.CuentaContableRepository;
import com.bcsystems.contabilidad.repository.VentaRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final AsientoContableRepository asientoContableRepository;
    private final CuentaContableRepository cuentaContableRepository;

    public VentaServiceImpl(VentaRepository ventaRepository, 
                          AsientoContableRepository asientoContableRepository,
                          CuentaContableRepository cuentaContableRepository) {
        this.ventaRepository = ventaRepository;
        this.asientoContableRepository = asientoContableRepository;
        this.cuentaContableRepository = cuentaContableRepository;
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

        if (request.asientoId() == null) {
            AsientoContable asiento = crearAsientoAutomaticoVenta(venta);
            venta.setAsiento(asiento);
        } else {
            Optional<AsientoContable> asiento = asientoContableRepository.findById(request.asientoId());
            if (asiento.isPresent()) {
                venta.setAsiento(asiento.get());
            }
        }

        return ventaRepository.save(venta);
    }

    private AsientoContable crearAsientoAutomaticoVenta(Venta venta) {
        String codigoCuentaIngreso;
        String codigoCuentaCobro;

        if (venta.getTipoVenta() == TipoVenta.CONTADO) {
            codigoCuentaIngreso = "4101";
            codigoCuentaCobro = "1101";
        } else {
            codigoCuentaIngreso = "4102";
            codigoCuentaCobro = "1103";
        }

        Optional<CuentaContable> cuentaIngreso = cuentaContableRepository.findByCodigo(codigoCuentaIngreso);
        Optional<CuentaContable> cuentaCobro = cuentaContableRepository.findByCodigo(codigoCuentaCobro);

        if (cuentaIngreso.isEmpty() || cuentaCobro.isEmpty()) {
            throw new NotFoundException("Cuentas contables no encontradas para venta automática");
        }

        List<MovimientoContable> movimientos = new ArrayList<>();

        MovimientoContable movDebe = MovimientoContable.builder()
                .debe(venta.getTotal())
                .haber(java.math.BigDecimal.ZERO)
                .cuentaContable(cuentaCobro.get())
                .build();
        movimientos.add(movDebe);

        MovimientoContable movHaber = MovimientoContable.builder()
                .debe(java.math.BigDecimal.ZERO)
                .haber(venta.getTotal())
                .cuentaContable(cuentaIngreso.get())
                .build();
        movimientos.add(movHaber);

        if (venta.getCosto() != null && venta.getCosto().compareTo(java.math.BigDecimal.ZERO) > 0) {
            Optional<CuentaContable> cuentaCosto = cuentaContableRepository.findByCodigo("5101");
            Optional<CuentaContable> cuentaAlmacen = cuentaContableRepository.findByCodigo("1105");

            if (cuentaCosto.isPresent() && cuentaAlmacen.isPresent()) {
                MovimientoContable movCostoDebe = MovimientoContable.builder()
                        .debe(venta.getCosto())
                        .haber(java.math.BigDecimal.ZERO)
                        .cuentaContable(cuentaCosto.get())
                        .build();
                movimientos.add(movCostoDebe);

                MovimientoContable movCostoHaber = MovimientoContable.builder()
                        .debe(java.math.BigDecimal.ZERO)
                        .haber(venta.getCosto())
                        .cuentaContable(cuentaAlmacen.get())
                        .build();
                movimientos.add(movCostoHaber);
            }
        }

        String referencia = "Venta-" + (venta.getTipoVenta() == TipoVenta.CONTADO ? "Contado" : "Credito") + "-" + LocalDate.now();
        String descripcion = "Venta registrada el " + venta.getFecha_venta() + " por total de " + venta.getTotal();

        AsientoContable asiento = AsientoContable.builder()
                .fecha(venta.getFecha_venta())
                .referencia(referencia)
                .descripcion(descripcion)
                .tipoAsientoContable(TipoAsientoContable.CONFIRMADO)
                .movimientos(new ArrayList<>())
                .build();

        asiento = asientoContableRepository.save(asiento);

        for (MovimientoContable mov : movimientos) {
            mov.setAsientoContable(asiento);
        }
        asiento.setMovimientos(movimientos);

        return asientoContableRepository.save(asiento);
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