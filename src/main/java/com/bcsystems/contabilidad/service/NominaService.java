package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.AsientoContable;
import com.bcsystems.contabilidad.domain.Comision;
import com.bcsystems.contabilidad.domain.CuentaContable;
import com.bcsystems.contabilidad.domain.Deduccion;
import com.bcsystems.contabilidad.domain.Usuario;
import com.bcsystems.contabilidad.domain.MovimientoContable;
import com.bcsystems.contabilidad.domain.Nomina;
import com.bcsystems.contabilidad.domain.en.EstadoNomina;
import com.bcsystems.contabilidad.domain.en.EstadoDeduccion;
import com.bcsystems.contabilidad.domain.en.OrigenDeduccion;
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
import com.bcsystems.contabilidad.repository.UsuarioRepository;
import com.bcsystems.contabilidad.repository.NominaRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageImpl;
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
    private final UsuarioRepository usuarioRepository;
    private final AsientoContableRepository asientoContableRepository;
    private final CuentaContableRepository cuentaContableRepository;
    private final ComisionRepository comisionRepository;
    private final DeduccionRepository deduccionRepository;
    private final ComisionService comisionService;
    private final DeduccionService deduccionService;
    private final AsistenciaService asistenciaService;
    private final PrestamoEmpleadoService prestamoEmpleadoService;

    public NominaService(NominaRepository nominaRepository,
                         UsuarioRepository usuarioRepository,
                         AsientoContableRepository asientoContableRepository,
                         CuentaContableRepository cuentaContableRepository,
                         ComisionRepository comisionRepository,
                         DeduccionRepository deduccionRepository,
                         ComisionService comisionService,
                         DeduccionService deduccionService,
                         AsistenciaService asistenciaService,
                         PrestamoEmpleadoService prestamoEmpleadoService) {
        this.nominaRepository = nominaRepository;
        this.usuarioRepository = usuarioRepository;
        this.asientoContableRepository = asientoContableRepository;
        this.cuentaContableRepository = cuentaContableRepository;
        this.comisionRepository = comisionRepository;
        this.deduccionRepository = deduccionRepository;
        this.comisionService = comisionService;
        this.deduccionService = deduccionService;
        this.asistenciaService = asistenciaService;
        this.prestamoEmpleadoService = prestamoEmpleadoService;
    }

    @Transactional
    public NominaResponse save(NominaRegisterRequest request) {
        if (request.usuarioId() == null) {
            throw new NotFoundException("Usuario requerido");
        }
        Usuario usuario = usuarioRepository.findById(request.usuarioId().intValue())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        LocalDate fechaPago = request.fecha_nomina() != null ? request.fecha_nomina() : LocalDate.now();
        LocalDate fechaInicio = request.fechaInicio() != null ? request.fechaInicio() : fechaPago.minusDays(6);
        LocalDate fechaFin = request.fechaFin() != null ? request.fechaFin() : fechaPago;
        validarPeriodo(fechaInicio, fechaFin);
        asistenciaService.generateAttendanceDeductions(usuario.getIdUser(), fechaInicio, fechaFin);
        prestamoEmpleadoService.generarDeduccionesParaNomina(usuario.getIdUser(), fechaFin);

        List<Long> requestedComisionIds = request.comisionIds() == null ? List.of() : request.comisionIds();
        List<Comision> comisiones = requestedComisionIds.isEmpty()
                ? comisionService.findPendientesByUsuario(usuario.getIdUser().longValue())
                : comisionRepository.findAllById(requestedComisionIds);
        comisiones = comisiones.stream().filter(c -> dentroDelPeriodo(c.getFechaComision(), fechaInicio, fechaFin)).toList();
        if (comisiones.size() != requestedComisionIds.stream().distinct().count() || comisiones.stream().anyMatch(c ->
                c.getUsuario() == null || !c.getUsuario().getIdUser().equals(usuario.getIdUser()) || c.getEstadoComision() != com.bcsystems.contabilidad.domain.en.EstadoComision.PENDIENTE)) {
            throw new IllegalArgumentException("Las comisiones seleccionadas no pertenecen al empleado o ya fueron aplicadas");
        }

        List<Deduccion> disponibles = deduccionRepository.findByUsuarioIdUserAndEstadoIn(usuario.getIdUser(),
                List.of(EstadoDeduccion.PENDIENTE, EstadoDeduccion.APROBADA));
        disponibles = disponibles.stream().filter(d -> dentroDelPeriodo(d.getFecha(), fechaInicio, fechaFin)).toList();
        List<Long> requestedDeductionIds = request.deduccionIds() == null ? List.of() : request.deduccionIds();
        boolean includeAllDeductions = requestedDeductionIds.isEmpty();
        disponibles.stream()
                .filter(d -> (includeAllDeductions || requestedDeductionIds.contains(d.getIdDeduccion()))
                        && d.getEstado() == EstadoDeduccion.PENDIENTE
                        && d.getOrigen() == OrigenDeduccion.ASISTENCIA)
                .forEach(d -> d.setEstado(EstadoDeduccion.APROBADA));
        List<Deduccion> deducciones = requestedDeductionIds.isEmpty()
                ? disponibles.stream().filter(this::puedeAplicarse).toList()
                : deduccionRepository.findAllById(requestedDeductionIds);
        if (deducciones.size() != requestedDeductionIds.stream().distinct().count() || deducciones.stream().anyMatch(d ->
                d.getUsuario() == null || !d.getUsuario().getIdUser().equals(usuario.getIdUser()) || !puedeAplicarse(d))) {
            throw new IllegalArgumentException("Las deducciones seleccionadas no pertenecen al empleado, no están aprobadas o ya fueron aplicadas");
        }
        List<Long> deductionIdsToApply = deducciones.stream().map(Deduccion::getIdDeduccion).toList();

        BigDecimal sueldoBase = usuario.getSueldoSemanal() != null ? usuario.getSueldoSemanal() : BigDecimal.ZERO;
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
                .fecha_nomina(fechaPago)
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .sueldo(sueldo)
                .sueldoBase(sueldoBase)
                .totalComisiones(totalComisiones)
                .totalDeducciones(totalDeducciones)
                .estado(EstadoNomina.GENERADA)
                .usuario(usuario)
                .build();
        nomina = nominaRepository.save(nomina);

        comisionService.pagarComisionesPorNomina(comisiones.stream().map(Comision::getIdComision).toList(), nomina);
        deduccionService.aplicarDeduccionesPorNomina(deductionIdsToApply, nomina);

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

        String nombreEmpleado = nomina.getUsuario() != null && nomina.getUsuario().getPersona() != null
                ? nomina.getUsuario().getPersona().getNombre() : "General";
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

    private boolean puedeAplicarse(Deduccion deduccion) {
        return deduccion.getEstado() == EstadoDeduccion.PENDIENTE || deduccion.getEstado() == EstadoDeduccion.APROBADA;
    }

    @Transactional
    public Page<NominaResponse> findAllPageable(PageRequest pageRequest) {
        Page<Nomina> nominas = nominaRepository.findAll(pageRequest);
        return nominas.map(NominaResponse::fromEntity);
    }

    public Page<NominaResponse> findFiltered(PageRequest request, String search, EstadoNomina estado, LocalDate desde, LocalDate hasta) {
        List<Nomina> filtered = nominaRepository.findAll(request.getSort()).stream().filter(n ->
                (estado == null || n.getEstado() == estado)
                        && (desde == null || n.getFecha_nomina() == null || !n.getFecha_nomina().isBefore(desde))
                        && (hasta == null || n.getFecha_nomina() == null || !n.getFecha_nomina().isAfter(hasta))
                        && (search == null || search.isBlank() || (n.getUsuario() != null && n.getUsuario().getPersona() != null
                        && (n.getUsuario().getPersona().getNombre() + " " + n.getUsuario().getPersona().getPrimerApellido()).toLowerCase().contains(search.toLowerCase())))).toList();
        int start = Math.min(request.getPageNumber() * request.getPageSize(), filtered.size());
        int end = Math.min(start + request.getPageSize(), filtered.size());
        return new PageImpl<>(filtered.subList(start, end).stream().map(NominaResponse::fromEntity).toList(), request, filtered.size());
    }

    @Transactional
    public NominaResponse findById(Long id) {
        Nomina nomina = nominaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Nómina no encontrada"));
        return NominaResponse.fromEntity(nomina);
    }

    @Transactional
    public NominaDesgloseResponse desgloseNomina(Long id) {
        Nomina nomina = nominaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Nómina no encontrada"));
        List<ComisionResponse> comisiones = comisionRepository.findByNominaIdNomina(nomina.getIdNomina())
                .stream().map(comisionService::toResponse).toList();
        List<DeduccionResponse> deducciones = deduccionRepository.findByNominaIdNomina(nomina.getIdNomina())
                .stream().map(DeduccionResponse::fromEntity).toList();
        return new NominaDesgloseResponse(
                nomina.getUsuario() == null ? null : nomina.getUsuario().getIdUser().longValue(),
                nomina.getUsuario() != null && nomina.getUsuario().getPersona() != null
                        ? nomina.getUsuario().getPersona().getNombre() : null,
                nomina.getSueldoBase(), nomina.getSueldoBase(), comisiones, deducciones,
                nomina.getTotalComisiones(), nomina.getTotalDeducciones());
    }

    @Transactional
    public NominaResponse update(Long id, NominaUpdateRequest request) {
        Nomina nomina = nominaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Nómina no encontrada"));
        if (request.fecha_nomina() != null) {
            nomina.setFecha_nomina(request.fecha_nomina());
        }
        LocalDate fechaInicio = request.fechaInicio() != null ? request.fechaInicio() : nomina.getFechaInicio();
        LocalDate fechaFin = request.fechaFin() != null ? request.fechaFin() : nomina.getFechaFin();
        if (fechaInicio != null || fechaFin != null) {
            validarPeriodo(fechaInicio, fechaFin);
            nomina.setFechaInicio(fechaInicio);
            nomina.setFechaFin(fechaFin);
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

    public NominaDesgloseResponse desglose(Integer usuarioId, LocalDate fechaInicio, LocalDate fechaFin) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        LocalDate inicio = fechaInicio == null ? LocalDate.now().minusDays(6) : fechaInicio;
        LocalDate fin = fechaFin == null ? LocalDate.now() : fechaFin;
        validarPeriodo(inicio, fin);

        prestamoEmpleadoService.generarDeduccionesParaNomina(usuarioId, fin);
        try { asistenciaService.generateAttendanceDeductions(usuarioId, inicio, fin); } catch (NotFoundException ignored) { }

        List<ComisionResponse> comisiones = comisionService.findPendientesByUsuario(usuarioId.longValue())
                .stream().map(comisionService::toResponse).toList();
        comisiones = comisiones.stream().filter(c -> dentroDelPeriodo(c.fechaGeneracion(), inicio, fin)).toList();
        List<DeduccionResponse> deducciones = deduccionService.findPendientesByUsuarioResponse(usuarioId).stream()
                .filter(d -> d.estado() == EstadoDeduccion.PENDIENTE || d.estado() == EstadoDeduccion.APROBADA)
                .filter(d -> dentroDelPeriodo(d.fecha(), inicio, fin))
                .toList();

        BigDecimal totalComisiones = comisiones.stream()
                .map(ComisionResponse::montoComision)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDeducciones = deducciones.stream()
                .map(DeduccionResponse::monto)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new NominaDesgloseResponse(
                usuarioId.longValue(),
                usuario.getPersona() != null ? usuario.getPersona().getNombre() : null,
                usuario.getSueldoSemanal(),
                usuario.getSueldoSemanal(),
                comisiones,
                deducciones,
                totalComisiones,
                totalDeducciones
        );
    }

    private void validarPeriodo(LocalDate inicio, LocalDate fin) {
        if (inicio == null || fin == null || inicio.isAfter(fin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha final");
        }
    }

    private boolean dentroDelPeriodo(LocalDate fecha, LocalDate inicio, LocalDate fin) {
        return fecha != null && !fecha.isBefore(inicio) && !fecha.isAfter(fin);
    }
}
