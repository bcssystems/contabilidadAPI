package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Comision;
import com.bcsystems.contabilidad.domain.Empleado;
import com.bcsystems.contabilidad.domain.Venta;
import com.bcsystems.contabilidad.domain.en.EstadoComision;
import com.bcsystems.contabilidad.domain.en.TipoComision;
import com.bcsystems.contabilidad.dto.ComisionRegisterRequest;
import com.bcsystems.contabilidad.dto.ComisionResponse;
import com.bcsystems.contabilidad.dto.ComisionUpdateRequest;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.ComisionRepository;
import com.bcsystems.contabilidad.repository.EmpleadoRepository;
import com.bcsystems.contabilidad.repository.VentaRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ComisionService {

    private final ComisionRepository comisionRepository;
    private final VentaRepository ventaRepository;
    private final EmpleadoRepository empleadoRepository;

    public ComisionService(ComisionRepository comisionRepository,
                          VentaRepository ventaRepository,
                          EmpleadoRepository empleadoRepository) {
        this.comisionRepository = comisionRepository;
        this.ventaRepository = ventaRepository;
        this.empleadoRepository = empleadoRepository;
    }

    @Transactional
    public Comision save(ComisionRegisterRequest request) {
        Optional<Venta> venta = ventaRepository.findById(request.ventaId().intValue());
        if (venta.isEmpty()) {
            throw new NotFoundException("Venta no encontrada");
        }

        Optional<Empleado> empleado = empleadoRepository.findById(request.empleadoId());
        if (empleado.isEmpty()) {
            throw new NotFoundException("Empleado no encontrado");
        }

        BigDecimal montoComision = BigDecimal.ZERO;
        if (request.montoBase() != null && request.porcentajeComision() != null) {
            montoComision = request.montoBase()
                    .multiply(request.porcentajeComision())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        Comision comision = Comision.builder()
                .tipoComision(request.tipoComision())
                .venta(venta.get())
                .empleado(empleado.get())
                .montoBase(request.montoBase())
                .porcentajeComision(request.porcentajeComision())
                .montoComision(montoComision)
                .estadoComision(EstadoComision.PENDIENTE)
                .fechaComision(LocalDate.now())
                .build();

        return comisionRepository.save(comision);
    }

    @Transactional
    public List<Comision> generateComisionesVenta(Long ventaId, Long empleadoVendedorId, 
                                                   Long empleadoCobradorId,
                                                   BigDecimal porcentajeVenta, 
                                                   BigDecimal porcentajeCobranza) {
        Optional<Venta> ventaOpt = ventaRepository.findById(ventaId.intValue());
        if (ventaOpt.isEmpty()) {
            throw new NotFoundException("Venta no encontrada");
        }

        Venta venta = ventaOpt.get();
        List<Comision> comisiones = new java.util.ArrayList<>();

        Optional<Empleado> vendedor = empleadoRepository.findById(empleadoVendedorId);
        if (vendedor.isPresent()) {
            BigDecimal montoComisionVenta = BigDecimal.valueOf(venta.getTotal())
                    .multiply(porcentajeVenta)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            Comision comisionVenta = Comision.builder()
                    .tipoComision(TipoComision.VENTA)
                    .venta(venta)
                    .empleado(vendedor.get())
                    .montoBase(BigDecimal.valueOf(venta.getTotal()))
                    .porcentajeComision(porcentajeVenta)
                    .montoComision(montoComisionVenta)
                    .estadoComision(EstadoComision.PENDIENTE)
                    .fechaComision(LocalDate.now())
                    .build();
            comisiones.add(comisionRepository.save(comisionVenta));
        }

        Optional<Empleado> cobrador = empleadoRepository.findById(empleadoCobradorId);
        if (cobrador.isPresent()) {
            BigDecimal montoComisionCobranza = BigDecimal.valueOf(venta.getTotal())
                    .multiply(porcentajeCobranza)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            Comision comisionCobranza = Comision.builder()
                    .tipoComision(TipoComision.COBRANZA)
                    .venta(venta)
                    .empleado(cobrador.get())
                    .montoBase(BigDecimal.valueOf(venta.getTotal()))
                    .porcentajeComision(porcentajeCobranza)
                    .montoComision(montoComisionCobranza)
                    .estadoComision(EstadoComision.PENDIENTE)
                    .fechaComision(LocalDate.now())
                    .build();
            comisiones.add(comisionRepository.save(comisionCobranza));
        }

        return comisiones;
    }

    @Transactional
    public List<Comision> findAll() {
        return comisionRepository.findAll();
    }

    public Page<ComisionResponse> findAllPageable(PageRequest pageRequest) {
        Page<Comision> comisiones = comisionRepository.findAll(pageRequest);
        return comisiones.map(this::toResponse);
    }

    private ComisionResponse toResponse(Comision comision) {
        return new ComisionResponse(
                comision.getIdComision(),
                comision.getTipoComision(),
                comision.getMontoBase(),
                comision.getPorcentajeComision(),
                comision.getMontoComision(),
                comision.getEstadoComision(),
                comision.getFechaComision(),
                comision.getFechaPago(),
                comision.getEmpleado() != null ? comision.getEmpleado().getIdEmpleado().longValue() : null,
                comision.getVenta() != null ? comision.getVenta().getIdVenta().longValue() : null
        );
    }

    public Optional<Comision> findById(Long id) {
        Optional<Comision> comision = comisionRepository.findById(id);
        if (comision.isPresent()) return comision;
        throw new NotFoundException("Comisión no encontrada");
    }

    public Optional<Comision> findById(Integer id) {
        return findById(id.longValue());
    }

    @Transactional
    public Comision update(Long id, ComisionUpdateRequest request) {
        Optional<Comision> comisionOpt = comisionRepository.findById(id);
        if (comisionOpt.isEmpty()) {
            throw new NotFoundException("Comisión no encontrada");
        }

        Comision comision = comisionOpt.get();

        if (request.tipoComision() != null) {
            comision.setTipoComision(request.tipoComision());
        }
        if (request.estadoComision() != null) {
            comision.setEstadoComision(request.estadoComision());
            if (request.estadoComision() == EstadoComision.PAGADA) {
                comision.setFechaPago(LocalDate.now());
            }
        }
        if (request.montoBase() != null) {
            comision.setMontoBase(request.montoBase());
        }
        if (request.porcentajeComision() != null) {
            comision.setPorcentajeComision(request.porcentajeComision());
        }
        if (request.montoComision() != null) {
            comision.setMontoComision(request.montoComision());
        }
        if (request.fechaPago() != null) {
            comision.setFechaPago(request.fechaPago());
        }

        return comisionRepository.save(comision);
    }

    @Transactional
    public void delete(Long id) {
        Optional<Comision> comisionOpt = comisionRepository.findById(id);
        if (comisionOpt.isEmpty()) {
            throw new NotFoundException("Comisión no encontrada");
        }
        comisionRepository.delete(comisionOpt.get());
    }

    public List<Comision> findByVenta(Long ventaId) {
        return comisionRepository.findByVentaIdVenta(ventaId);
    }

    public List<Comision> findByEmpleado(Long empleadoId) {
        return comisionRepository.findByEmpleadoIdEmpleado(empleadoId);
    }

    public List<Comision> findPendientes() {
        return comisionRepository.findByEstadoComision(EstadoComision.PENDIENTE);
    }

    public List<Comision> findPendientesByEmpleado(Long empleadoId) {
        return comisionRepository.findByEmpleadoIdEmpleadoAndEstadoComision(empleadoId, EstadoComision.PENDIENTE);
    }

    @Transactional
    public List<Comision> pagarComisiones(List<Long> comisionIds) {
        List<Comision> comisiones = new java.util.ArrayList<>();
        for (Long id : comisionIds) {
            Optional<Comision> comisionOpt = comisionRepository.findById(id);
            if (comisionOpt.isPresent()) {
                Comision comision = comisionOpt.get();
                comision.setEstadoComision(EstadoComision.PAGADA);
                comision.setFechaPago(LocalDate.now());
                comisiones.add(comisionRepository.save(comision));
            }
        }
        return comisiones;
    }
}
