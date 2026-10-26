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
import com.bcsystems.contabilidad.dto.NominaBatchRequest;
import com.bcsystems.contabilidad.dto.NominaBatchResponse;
import com.bcsystems.contabilidad.dto.NominaReviewRequest;
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
import java.util.Set;

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
    public NominaBatchResponse prepararLote(NominaBatchRequest request) {
        validarPeriodo(request.fechaInicio(), request.fechaFin());
        LocalDate fechaPago = request.fechaPago() == null ? LocalDate.now() : request.fechaPago();
        validarPeriodoSemanal(request.fechaInicio(), request.fechaFin(), fechaPago);
        List<Nomina> nominas = new ArrayList<>();
        for (Usuario usuario : usuarioRepository.findAll().stream().filter(u -> Boolean.TRUE.equals(u.getActivo())).toList()) {
            try { asistenciaService.generateAttendanceDeductions(usuario.getIdUser(), request.fechaInicio(), request.fechaFin()); }
            catch (NotFoundException ignored) { }
            prestamoEmpleadoService.generarDeduccionesParaNomina(usuario.getIdUser(), request.fechaFin());
            validarPeriodoNoPagado(usuario.getIdUser(), request.fechaInicio(), request.fechaFin());
            Nomina nomina = nominaRepository.findFirstByUsuarioIdUserAndFechaInicioAndFechaFinAndEstadoIn(
                    usuario.getIdUser(), request.fechaInicio(), request.fechaFin(),
                    List.of(EstadoNomina.PENDIENTE_REVISION, EstadoNomina.REVISADA))
                    .orElseGet(() -> nominaRepository.save(Nomina.builder()
                            .fecha_nomina(fechaPago).fechaInicio(request.fechaInicio()).fechaFin(request.fechaFin())
                            .sueldoBase(usuario.getSueldoSemanal()).sueldo(usuario.getSueldoSemanal())
                            .totalComisiones(BigDecimal.ZERO).totalDeducciones(BigDecimal.ZERO)
                     .estado(EstadoNomina.PENDIENTE_REVISION).usuario(usuario).build()));
            if (nomina.getFecha_nomina() == null) nomina.setFecha_nomina(fechaPago);
            if (nomina.getEstado() == EstadoNomina.PENDIENTE_REVISION) {
                List<Comision> commissions = comisionRepository.findByUsuarioIdUser(usuario.getIdUser()).stream()
                        .filter(item -> item.getNomina() == null && item.getEstadoComision() == com.bcsystems.contabilidad.domain.en.EstadoComision.PENDIENTE)
                        .filter(item -> dentroDelPeriodo(item.getFechaComision(), request.fechaInicio(), request.fechaFin())).toList();
                List<Deduccion> deductions = deduccionRepository.findByUsuarioIdUserAndEstadoIn(usuario.getIdUser(), List.of(EstadoDeduccion.PENDIENTE, EstadoDeduccion.APROBADA)).stream()
                        .filter(item -> item.getNomina() == null && dentroDelPeriodo(item.getFecha(), request.fechaInicio(), request.fechaFin())).toList();
                actualizarTotales(nomina, commissions, deductions);
            }
            nominas.add(nominaRepository.save(nomina));
        }
        return resumenLote(nominas);
    }

    @Transactional
    public NominaResponse revisar(Long id, NominaReviewRequest request) {
        Nomina nomina = nominaRepository.findById(id).orElseThrow(() -> new NotFoundException("Nómina no encontrada"));
        if (nomina.getEstado() != EstadoNomina.PENDIENTE_REVISION && nomina.getEstado() != EstadoNomina.REVISADA) {
            throw new IllegalArgumentException("La nómina ya no está disponible para revisión");
        }
        comisionRepository.findByNominaIdNomina(id).forEach(item -> { item.setNomina(null); comisionRepository.save(item); });
        deduccionRepository.findByNominaIdNomina(id).forEach(item -> { item.setNomina(null); deduccionRepository.save(item); });
        List<Long> commissionIds = request == null || request.comisionIds() == null ? List.of() : request.comisionIds();
        List<Long> deductionIds = request == null || request.deduccionIds() == null ? List.of() : request.deduccionIds();
        Set<Long> uniqueCommissionIds = Set.copyOf(commissionIds);
        Set<Long> uniqueDeductionIds = Set.copyOf(deductionIds);
        List<Comision> commissions = comisionRepository.findAllById(uniqueCommissionIds);
        List<Deduccion> deductions = deduccionRepository.findAllById(uniqueDeductionIds);
        if (commissions.size() != uniqueCommissionIds.size() || commissions.stream().anyMatch(item -> (item.getNomina() != null && !item.getNomina().getIdNomina().equals(nomina.getIdNomina())) || !belongsToPeriod(item.getUsuario(), nomina) || item.getEstadoComision() != com.bcsystems.contabilidad.domain.en.EstadoComision.PENDIENTE)) {
            throw new IllegalArgumentException("Hay comisiones inválidas para esta nómina");
        }
        if (deductions.size() != uniqueDeductionIds.size() || deductions.stream().anyMatch(item -> (item.getNomina() != null && !item.getNomina().getIdNomina().equals(nomina.getIdNomina())) || !belongsToPeriod(item.getUsuario(), nomina) || !dentroDelPeriodo(item.getFecha(), nomina.getFechaInicio(), nomina.getFechaFin()) || !puedeAplicarse(item))) {
            throw new IllegalArgumentException("Hay deducciones inválidas para esta nómina");
        }
        commissions.forEach(item -> { item.setNomina(nomina); comisionRepository.save(item); });
        deductions.forEach(item -> { if (item.getOrigen() == OrigenDeduccion.ASISTENCIA && item.getEstado() == EstadoDeduccion.PENDIENTE) item.setEstado(EstadoDeduccion.APROBADA); item.setNomina(nomina); deduccionRepository.save(item); });
        actualizarTotales(nomina, commissions, deductions);
        nomina.setEstado(EstadoNomina.REVISADA);
        return NominaResponse.fromEntity(nominaRepository.save(nomina));
    }

    @Transactional
    public NominaBatchResponse generarLote(List<Long> ids) {
        List<Nomina> nominas = ids == null || ids.isEmpty() ? nominaRepository.findAll() : nominaRepository.findAllById(ids);
        if (nominas.stream().anyMatch(item -> item.getEstado() != EstadoNomina.REVISADA)) {
            throw new IllegalArgumentException("Todas las nóminas seleccionadas deben estar revisadas");
        }
        if (nominas.isEmpty()) throw new IllegalArgumentException("No hay nóminas revisadas para generar");
        List<Nomina> generated = nominas.stream().map(this::finalizarNomina).toList();
        return resumenLote(generated);
    }

    private Nomina finalizarNomina(Nomina nomina) {
        List<Comision> commissions = comisionRepository.findByNominaIdNomina(nomina.getIdNomina());
        List<Deduccion> deductions = deduccionRepository.findByNominaIdNomina(nomina.getIdNomina());
        BigDecimal commissionsTotal = commissions.stream().map(Comision::getMontoComision).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal deductionsTotal = deductions.stream().map(Deduccion::getMonto).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        nomina.setTotalComisiones(commissionsTotal);
        nomina.setTotalDeducciones(deductionsTotal);
        nomina.setSueldo((nomina.getSueldoBase() == null ? BigDecimal.ZERO : nomina.getSueldoBase()).add(commissionsTotal).subtract(deductionsTotal));
        nominaRepository.save(nomina);
        comisionService.pagarComisionesPorNomina(commissions.stream().map(Comision::getIdComision).toList(), nomina);
        deduccionService.aplicarDeduccionesPorNomina(deductions.stream().map(Deduccion::getIdDeduccion).toList(), nomina);
        nomina.setEstado(EstadoNomina.GENERADA);
        nomina.setAsientoContable(crearAsientoAutomaticoNomina(nomina));
        return nominaRepository.save(nomina);
    }

    private boolean belongsToPeriod(Usuario usuario, Nomina nomina) {
        return usuario != null && nomina.getUsuario() != null && usuario.getIdUser().equals(nomina.getUsuario().getIdUser());
    }

    private void actualizarTotales(Nomina nomina, List<?> commissions, List<?> deductions) {
        BigDecimal commissionTotal = commissions.stream().map(item -> item instanceof Comision c ? c.getMontoComision() : BigDecimal.ZERO).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal deductionTotal = deductions.stream().map(item -> item instanceof Deduccion d ? d.getMonto() : BigDecimal.ZERO).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        nomina.setTotalComisiones(commissionTotal); nomina.setTotalDeducciones(deductionTotal);
        nomina.setSueldo((nomina.getSueldoBase() == null ? BigDecimal.ZERO : nomina.getSueldoBase()).add(commissionTotal).subtract(deductionTotal));
    }

    private NominaBatchResponse resumenLote(List<Nomina> nominas) {
        BigDecimal base = nominas.stream().map(Nomina::getSueldoBase).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal commissions = nominas.stream().map(Nomina::getTotalComisiones).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal deductions = nominas.stream().map(Nomina::getTotalDeducciones).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new NominaBatchResponse(nominas.stream().map(NominaResponse::fromEntity).toList(), base, commissions, deductions, base.add(commissions).subtract(deductions));
    }

    @Transactional
    public NominaResponse save(NominaRegisterRequest request) {
        if (request.usuarioId() == null) {
            throw new NotFoundException("Usuario requerido");
        }
        Usuario usuario = usuarioRepository.findById(request.usuarioId().intValue())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        if (!Boolean.TRUE.equals(usuario.getActivo())) throw new IllegalArgumentException("No se puede generar nómina para un usuario inactivo");
        LocalDate fechaPago = request.fecha_nomina() != null ? request.fecha_nomina() : LocalDate.now();
        LocalDate fechaInicio = request.fechaInicio() != null ? request.fechaInicio() : fechaPago.minusDays(6);
        LocalDate fechaFin = request.fechaFin() != null ? request.fechaFin() : fechaPago;
        validarPeriodo(fechaInicio, fechaFin);
        validarPeriodoSemanal(fechaInicio, fechaFin, fechaPago);
        validarPeriodoNoPagado(usuario.getIdUser(), fechaInicio, fechaFin);
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
                : deduccionRepository.findAllById(requestedDeductionIds).stream()
                .filter(d -> dentroDelPeriodo(d.getFecha(), fechaInicio, fechaFin)).toList();
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
        validarPeriodoSemanal(inicio, fin, fin.plusDays(1));

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

    private void validarPeriodoSemanal(LocalDate inicio, LocalDate fin, LocalDate pago) {
        if (inicio.getDayOfWeek() != java.time.DayOfWeek.FRIDAY || fin.getDayOfWeek() != java.time.DayOfWeek.THURSDAY
                || !fin.equals(inicio.plusDays(6)) || pago == null || !pago.equals(fin.plusDays(1))
                || pago.getDayOfWeek() != java.time.DayOfWeek.FRIDAY) {
            throw new IllegalArgumentException("La nómina debe cubrir de viernes a jueves y pagarse el viernes siguiente");
        }
    }

    private void validarPeriodoNoPagado(Integer usuarioId, LocalDate inicio, LocalDate fin) {
        validarPeriodoNoPagado(usuarioId, inicio, fin, null);
    }

    private void validarPeriodoNoPagado(Integer usuarioId, LocalDate inicio, LocalDate fin, Long excluirId) {
        boolean overlaps = nominaRepository.findAll().stream()
                .filter(item -> item.getUsuario() != null && item.getUsuario().getIdUser().equals(usuarioId))
                .filter(item -> item.getEstado() == EstadoNomina.GENERADA)
                .filter(item -> excluirId == null || !item.getIdNomina().equals(excluirId))
                .anyMatch(item -> item.getFechaInicio() != null && item.getFechaFin() != null
                        && !fin.isBefore(item.getFechaInicio()) && !inicio.isAfter(item.getFechaFin()));
        if (overlaps) throw new IllegalArgumentException("El periodo contiene días de una nómina ya pagada para este empleado");
    }

    private boolean dentroDelPeriodo(LocalDate fecha, LocalDate inicio, LocalDate fin) {
        return fecha != null && !fecha.isBefore(inicio) && !fecha.isAfter(fin);
    }
}
