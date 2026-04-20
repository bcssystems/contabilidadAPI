package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.AsientoContable;
import com.bcsystems.contabilidad.domain.CuentaContable;
import com.bcsystems.contabilidad.domain.Empleado;
import com.bcsystems.contabilidad.domain.MovimientoContable;
import com.bcsystems.contabilidad.domain.Nomina;
import com.bcsystems.contabilidad.domain.en.TipoAsientoContable;
import com.bcsystems.contabilidad.dto.NominaRegisterRequest;
import com.bcsystems.contabilidad.dto.NominaResponse;
import com.bcsystems.contabilidad.dto.NominaUpdateRequest;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.AsientoContableRepository;
import com.bcsystems.contabilidad.repository.CuentaContableRepository;
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
import java.util.Optional;

@Service
public class NominaService {

    private final NominaRepository nominaRepository;
    private final EmpleadoRepository empleadoRepository;
    private final AsientoContableRepository asientoContableRepository;
    private final CuentaContableRepository cuentaContableRepository;

    public NominaService(NominaRepository nominaRepository,
                      EmpleadoRepository empleadoRepository,
                      AsientoContableRepository asientoContableRepository,
                      CuentaContableRepository cuentaContableRepository) {
        this.nominaRepository = nominaRepository;
        this.empleadoRepository = empleadoRepository;
        this.asientoContableRepository = asientoContableRepository;
        this.cuentaContableRepository = cuentaContableRepository;
    }

    @Transactional
    public Nomina save(NominaRegisterRequest request) {
        Nomina nomina = Nomina.builder()
                .fecha_nomina(request.fecha_nomina())
                .sueldo(request.sueldo())
                .build();

        if (request.empleadoId() != null) {
            Optional<Empleado> empleado = empleadoRepository.findById(request.empleadoId());
            if (empleado.isPresent()) {
                nomina.setEmpleado(empleado.get());
            }
        }

        if (request.asientoId() == null) {
            AsientoContable asiento = crearAsientoAutomaticoNomina(nomina);
            nomina.setAsientoContable(asiento);
        } else {
            Optional<AsientoContable> asiento = asientoContableRepository.findById(request.asientoId());
            if (asiento.isPresent()) {
                nomina.setAsientoContable(asiento.get());
            }
        }

        return nominaRepository.save(nomina);
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

    public List<Nomina> findAll() {
        return nominaRepository.findAll();
    }

    public Page<NominaResponse> findAllPageable(PageRequest pageRequest) {
        Page<Nomina> nominas = nominaRepository.findAll(pageRequest);
        return nominas.map(this::toResponse);
    }

    private NominaResponse toResponse(Nomina nomina) {
        return new NominaResponse(
                nomina.getIdNomina(),
                nomina.getFecha_nomina(),
                nomina.getSueldo(),
                nomina.getEmpleado() != null ? nomina.getEmpleado().getIdEmpleado().longValue() : null,
                nomina.getAsientoContable() != null ? nomina.getAsientoContable().getIdAsientoContable() : null
        );
    }

    public Optional<Nomina> findById(Long id) {
        Optional<Nomina> nomina = nominaRepository.findById(id);
        if (nomina.isPresent()) return nomina;
        throw new NotFoundException("Nómina no encontrada");
    }

    @Transactional
    public Nomina update(Long id, NominaUpdateRequest request) {
        Optional<Nomina> nominaOpt = nominaRepository.findById(id);
        if (!nominaOpt.isPresent()) {
            throw new NotFoundException("Nómina no encontrada");
        }

        Nomina nomina = nominaOpt.get();

        if (request.fecha_nomina() != null) {
            nomina.setFecha_nomina(request.fecha_nomina());
        }
        if (request.sueldo() != null) {
            nomina.setSueldo(request.sueldo());
        }
        if (request.empleadoId() != null) {
            Optional<Empleado> empleado = empleadoRepository.findById(request.empleadoId());
            if (empleado.isPresent()) {
                nomina.setEmpleado(empleado.get());
            }
        }
        if (request.asientoId() != null) {
            Optional<AsientoContable> asiento = asientoContableRepository.findById(request.asientoId());
            if (asiento.isPresent()) {
                nomina.setAsientoContable(asiento.get());
            }
        }

        return nominaRepository.save(nomina);
    }

    @Transactional
    public void delete(Long id) {
        Optional<Nomina> nominaOpt = nominaRepository.findById(id);
        if (!nominaOpt.isPresent()) {
            throw new NotFoundException("Nómina no encontrada");
        }
        nominaRepository.delete(nominaOpt.get());
    }
}