package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.AsientoContable;
import com.bcsystems.contabilidad.domain.Comision;
import com.bcsystems.contabilidad.domain.CuentaContable;
import com.bcsystems.contabilidad.domain.Deduccion;
import com.bcsystems.contabilidad.domain.Empleado;
import com.bcsystems.contabilidad.domain.MovimientoContable;
import com.bcsystems.contabilidad.domain.Nomina;
import com.bcsystems.contabilidad.domain.en.EstadoNomina;
import com.bcsystems.contabilidad.domain.en.TipoAsientoContable;
import com.bcsystems.contabilidad.dto.ComisionResponse;
import com.bcsystems.contabilidad.dto.DeduccionResponse;
import com.bcsystems.contabilidad.dto.NominaDesgloseResponse;
import com.bcsystems.contabilidad.dto.NominaRegisterRequest;
import com.bcsystems.contabilidad.dto.NominaResponse;
import com.bcsystems.contabilidad.dto.NominaUpdateRequest;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.AsientoContableRepository;
import com.bcsystems.contabilidad.repository.ComisionRepository;
import com.bcsystems.contabilidad.repository.CuentaContableRepository;
import com.bcsystems.contabilidad.repository.DeduccionRepository;
import com.bcsystems.contabilidad.repository.EmpleadoRepository;
import com.bcsystems.contabilidad.repository.NominaRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class NominaService {

    private final NominaRepository nominaRepository;
    private final EmpleadoRepository empleadoRepository;
    private final AsientoContableRepository asientoContableRepository;
    private final CuentaContableRepository cuentaContableRepository;
    private final ComisionRepository comisionRepository;
    private final DeduccionRepository deduccionRepository;
    private final ComisionService comisionService;
    private final DeduccionService deduccionService;

    public NominaService(NominaRepository nominaRepository,
                         EmpleadoRepository empleadoRepository,
                         AsientoContableRepository asientoContableRepository,
                         CuentaContableRepository cuentaContableRepository,
                         ComisionRepository comisionRepository,
                         DeduccionRepository deduccionRepository,
                         ComisionService comisionService,
                         DeduccionService deduccionService) {
        this.nominaRepository = nominaRepository;
        this.empleadoRepository = empleadoRepository;
        this.asientoContableRepository = asientoContableRepository;
        this.cuentaContableRepository = cuentaContableRepository;
        this.comisionRepository = comisionRepository;
        this.deduccionRepository = deduccionRepository;
        this.comisionService = comisionService;
        this.deduccionService = deduccionService;
    }

    @Transactional
    public NominaResponse save(NominaRegisterRequest request) {
        if (request.empleadoId() == null) {
            throw new NotFoundException("Empleado requerido");
        }
        Empleado empleado = empleadoRepository.findById(request.empleadoId())
                .orElseThrow(() -> new NotFoundException("Empleado no encontrado"));

        List<Comision> comisiones = comisionRepository.findAllById(
                request.comisionIds() != null ? request.comisionIds() : List.of());
        List<Deduccion> deducciones = deduccionRepository.findAllById(
                request.deduccionIds() != null ? request.deduccionIds() : List.of());

        BigDecimal sueldoBase = empleado.getSueldoBase() != null ? empleado.getSueldoBase() : BigDecimal.ZERO;
        BigDecimal totalComisiones = comisiones.stream()
                .map(Comision::getMontoComision)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDeducciones = deducciones.stream()
                .map(Deduccion::getMonto)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal sueldo = sueldoBase.add(totalComisiones).subtract(totalDeducciones);

        Nomina nomina = Nomina.builder()
                .fecha_nomina(request.fecha_nomina() != null ? request.fecha_nomina() : LocalDate.now())
                .sueldo(sueldo)
                .sueldoBase(sueldoBase)
                .totalComisiones(totalComisiones)
                .totalDeducciones(totalDeducciones)
                .estado(EstadoNomina.GENERADA)
                .empleado(empleado)
                .build();
        nomina = nominaRepository.save(nomina);

        comisionService.pagarComisionesPorNomina(request.comisionIds(), nomina);
        deduccionService.aplicarDeduccionesPorNomina(request.deduccionIds(), nomina);

        AsientoContable asiento = crearAsientoAutomaticoNomina(nomina);
        nomina.setAsientoContable(asiento);
        nomina = nominaRepository.save(nomina);

        return NominaResponse.fromEntity(nomina);
    }

    private AsientoContable crearAsientoAutomaticoNomina(Nomina nomina) {
        Optional<CuentaContable> cuentaGasto = cuentaContableRepository.findByCodigo("6101");
        Optional<CuentaContable> cuentaPago = cuentaContableRepository.findByCodigo("1101");

        if (cuentaGasto.isEmpty() || cuentaPago.isEmpty()) {
            throw new NotFoundException("Cuentas contables no encontradas para nómina automática");
        }

        List<MovimientoContable> movimientos = new ArrayList<>();

        MovimientoContable movDebe = MovimientoContable.builder()
                .debe(nomina.getSueldo())
                .haber(BigDecimal.ZERO)
                .cuentaContable(cuentaGasto.get())
                .build();
        movimientos.add(movDebe);

        MovimientoContable movHaber = MovimientoContable.builder()
                .debe(BigDecimal.ZERO)
                .haber(nomina.getSueldo())
                .cuentaContable(cuentaPago.get())
                .build();
        movimientos.add(movHaber);

        String nombreEmpleado = nomina.getEmpleado() != null ? nomina.getEmpleado().getPersona().getNombre() : "General";
        String referencia = "Nomina-" + nombreEmpleado + "-" + LocalDate.now();
        String descripcion = "Pago de nómina a " + nombreEmpleado + " por " + nomina.getSueldo();

        AsientoContable asiento = AsientoContable.builder()
                .fecha(nomina.getFecha_nomina())
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

    @Transactional
    public Page<NominaResponse> findAllPageable(PageRequest pageRequest) {
        Page<Nomina> nominas = nominaRepository.findAll(pageRequest);
        return nominas.map(NominaResponse::fromEntity);
    }

    @Transactional
    public NominaResponse findById(Long id) {
        Nomina nomina = nominaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Nómina no encontrada"));
        return NominaResponse.fromEntity(nomina);
    }

    @Transactional
    public NominaResponse update(Long id, NominaUpdateRequest request) {
        Nomina nomina = nominaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Nómina no encontrada"));
        if (request.fecha_nomina() != null) {
            nomina.setFecha_nomina(request.fecha_nomina());
        }
        return NominaResponse.fromEntity(nominaRepository.save(nomina));
    }

    @Transactional
    public NominaResponse cancelar(Long id) {
        Nomina nomina = nominaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Nómina no encontrada"));
        if (nomina.getEstado() == EstadoNomina.CANCELADA) {
            throw new NotFoundException("La nómina ya está cancelada");
        }
        comisionService.revertirComisionesDeNomina(nomina);
        deduccionService.revertirDeduccionesDeNomina(nomina);
        nomina.setEstado(EstadoNomina.CANCELADA);
        nomina = nominaRepository.save(nomina);
        return NominaResponse.fromEntity(nomina);
    }

    @Transactional
    public void delete(Long id) {
        Nomina nomina = nominaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Nómina no encontrada"));
        comisionService.revertirComisionesDeNomina(nomina);
        deduccionService.revertirDeduccionesDeNomina(nomina);
        nominaRepository.delete(nomina);
    }

    @Transactional
    public NominaDesgloseResponse desglose(Integer empleadoId) {
        Empleado empleado = empleadoRepository.findById(empleadoId.longValue())
                .orElseThrow(() -> new NotFoundException("Empleado no encontrado"));

        List<ComisionResponse> comisiones = comisionService.findPendientesByEmpleado(empleadoId.longValue())
                .stream().map(comisionService::toResponse).toList();
        List<DeduccionResponse> deducciones = deduccionService.findPendientesByEmpleadoResponse(empleadoId);

        BigDecimal totalComisiones = comisiones.stream()
                .map(ComisionResponse::montoComision)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDeducciones = deducciones.stream()
                .map(DeduccionResponse::monto)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new NominaDesgloseResponse(
                empleadoId.longValue(),
                empleado.getPersona() != null ? empleado.getPersona().getNombre() : null,
                empleado.getSueldoBase(),
                comisiones,
                deducciones,
                totalComisiones,
                totalDeducciones
        );
    }
}
