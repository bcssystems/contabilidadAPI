package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.*;
import com.bcsystems.contabilidad.domain.en.*;
import com.bcsystems.contabilidad.dto.*;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class AsistenciaService {
    private final UsuarioService usuarios;
    private final UsuarioRepository usuariosRepository;
    private final AsistenciaRepository asistencias;
    private final MarcajeRepository marcajes;
    private final EvidenciaAsistenciaRepository evidencias;
    private final PuntoGeograficoRepository puntos;
    private final JustificacionAsistenciaRepository justificaciones;
    private final DeduccionRepository deduccionRepository;
    private final ConfiguracionAsistenciaService configuracion;
    private final HorarioService horarios;
    private final UsuarioHorarioRepository asignaciones;
    private final Path uploadDir;

    public AsistenciaService(UsuarioService usuarios, UsuarioRepository usuariosRepository,
                             AsistenciaRepository asistencias, MarcajeRepository marcajes,
                             EvidenciaAsistenciaRepository evidencias, PuntoGeograficoRepository puntos,
                             JustificacionAsistenciaRepository justificaciones,
                             ConfiguracionAsistenciaService configuracion, HorarioService horarios,
                             UsuarioHorarioRepository asignaciones, DeduccionRepository deduccionRepository,
                             @Value("${app.attendance.upload.dir:./uploads}") String uploadDir) {
        this.usuarios = usuarios;
        this.usuariosRepository = usuariosRepository;
        this.asistencias = asistencias;
        this.marcajes = marcajes;
        this.evidencias = evidencias;
        this.puntos = puntos;
        this.justificaciones = justificaciones;
        this.deduccionRepository = deduccionRepository;
        this.configuracion = configuracion;
        this.horarios = horarios;
        this.asignaciones = asignaciones;
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize().resolve("asistencias");
    }

    private Usuario usuario(String username) {
        return usuarios.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }

    @Transactional
    public MarcajeResponse mark(String username, TipoMarcaje type, MultipartFile selfie,
                                BigDecimal lat, BigDecimal lon, BigDecimal precision) {
        if (configuracion.get().getRequiereSelfie() && (selfie == null || selfie.isEmpty())) {
            throw new IllegalArgumentException("La selfie es obligatoria");
        }
        if (lat == null || lon == null) throw new IllegalArgumentException("La ubicacion es obligatoria");

        Usuario user = usuario(username);
        LocalDateTime now = LocalDateTime.now();
        LocalDate date = now.toLocalDate();
        UsuarioHorario assignment = asignaciones.findFirstByUsuarioIdUserAndActivoTrueOrderByIdDesc(user.getIdUser())
                .orElseThrow(() -> new IllegalArgumentException("No tienes un horario asignado. No puedes registrar asistencia."));
        DiaSemana scheduledDay = DiaSemana.values()[date.getDayOfWeek().getValue() - 1];
        HorarioDia todaySchedule = assignment.getHorario().getDias().stream()
                .filter(day -> day.getDia() == scheduledDay).findFirst().orElse(null);
        if (todaySchedule == null) {
            throw new IllegalArgumentException("Hoy no tienes jornada asignada. Es un día de descanso.");
        }
        Asistencia attendance = asistencias.findByUsuarioIdUserAndFecha(user.getIdUser(), date)
                .orElseGet(() -> asistencias.save(Asistencia.builder()
                        .usuario(user).fecha(date).estado(EstadoIncidencia.PENDIENTE).build()));
        if (type == TipoMarcaje.SALIDA && attendance.getEntrada() == null) {
            throw new IllegalArgumentException("No puedes registrar la salida sin una entrada registrada hoy");
        }
        if (marcajes.existsByAsistenciaIdAndTipo(attendance.getId(), type)) {
            throw new IllegalArgumentException("El marcaje ya existe para hoy");
        }
        if (type == TipoMarcaje.ENTRADA) attendance.setEntrada(now); else attendance.setSalida(now);

        PuntoGeografico closest = null;
        double distance = Double.MAX_VALUE;
        for (PuntoGeografico point : puntos.findByActivoTrue()) {
            double current = distance(lat.doubleValue(), lon.doubleValue(), point.getLatitud().doubleValue(), point.getLongitud().doubleValue());
            if (current < distance) { distance = current; closest = point; }
        }
        int configuredRadius = configuracion.get().getRadioMaximo();
        boolean inside = closest != null && distance <= Math.min(closest.getRadioMetros(), configuredRadius);
        Marcaje mark = marcajes.save(Marcaje.builder().asistencia(attendance).tipo(type).marcadoEn(now)
                .latitud(lat).longitud(lon).precisionMetros(precision)
                .distanciaMetros(closest == null ? null : distance)
                .estadoUbicacion(inside ? EstadoIncidencia.APROBADA : EstadoIncidencia.PENDIENTE)
                .puntoGeografico(closest).build());

        if (selfie != null && !selfie.isEmpty()) saveEvidence(selfie, mark, now);
        attendance.setEstado(inside ? EstadoIncidencia.APROBADA : EstadoIncidencia.PENDIENTE);
        if (!inside) attendance.setFueraDePunto(true);
        if (type == TipoMarcaje.ENTRADA) {
            long minutes = java.time.Duration.between(todaySchedule.getEntrada(), now.toLocalTime()).toMinutes();
            attendance.setMinutosRetardo((int) Math.max(0, minutes));
        } else {
            long minutes = java.time.Duration.between(now.toLocalTime(), todaySchedule.getSalida()).toMinutes();
            attendance.setMinutosSalidaTemprana((int) Math.max(0, minutes));
        }
        asistencias.save(attendance);
        return new MarcajeResponse(attendance.getId(), mark.getId(), type, now, attendance.getEntrada(),
                attendance.getSalida(), mark.getDistanciaMetros(), mark.getEstadoUbicacion().name(),
                attendance.getEstado().name(), !inside, closest == null ? null : closest.getNombre(),
                lat.doubleValue(), lon.doubleValue(), closest == null ? null : closest.getRadioMetros());
    }

    private void saveEvidence(MultipartFile selfie, Marcaje mark, LocalDateTime now) {
        try {
            Files.createDirectories(uploadDir);
            String original = selfie.getOriginalFilename() == null ? "selfie.jpg" : selfie.getOriginalFilename();
            String ext = original.contains(".") ? original.substring(original.lastIndexOf('.')).replaceAll("[^a-zA-Z0-9.]", "") : ".jpg";
            String filename = UUID.randomUUID() + ext;
            Path target = uploadDir.resolve(filename).normalize();
            if (!target.getParent().equals(uploadDir.toAbsolutePath().normalize())) throw new IOException("Ruta invalida");
            selfie.transferTo(target);
            evidencias.save(EvidenciaAsistencia.builder().marcaje(mark).nombreArchivo(filename)
                    .rutaArchivo(target.toString()).creadoEn(now).estado(EstadoEvidencia.PENDIENTE).build());
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo guardar la selfie", ex);
        }
    }

    public List<Asistencia> mine(String username) {
        return asistencias.findByUsuarioIdUserOrderByFechaDesc(usuario(username).getIdUser());
    }

    public List<Asistencia> all() { return asistencias.findAll(); }

    public List<EvidenciaRevisionResponse> pendingEvidence() {
        return evidencias.findByEstadoOrderByCreadoEnDesc(EstadoEvidencia.PENDIENTE).stream().map(this::evidenceResponse).toList();
    }

    public List<EvidenciaRevisionResponse> allEvidence() {
        return evidencias.findAllByOrderByCreadoEnDesc().stream().map(this::evidenceResponse).toList();
    }

    public Path evidencePath(Integer id) {
        EvidenciaAsistencia evidence = evidencias.findById(id).orElseThrow(() -> new NotFoundException("Evidencia no encontrada"));
        return uploadDir.resolve(evidence.getNombreArchivo()).normalize();
    }

    private EvidenciaRevisionResponse evidenceResponse(EvidenciaAsistencia evidence) {
        return new EvidenciaRevisionResponse(evidence.getId(), evidence.getMarcaje().getId(),
                evidence.getMarcaje().getAsistencia().getId(), evidence.getMarcaje().getAsistencia().getUsuario().getIdUser(),
                nombreUsuario(evidence.getMarcaje().getAsistencia().getUsuario()), evidence.getMarcaje().getTipo().name(),
                evidence.getMarcaje().getMarcadoEn(), evidence.getMarcaje().getDistanciaMetros(),
                evidence.getMarcaje().getEstadoUbicacion().name(), evidence.getEstado());
    }

    private String nombreUsuario(Usuario user) {
        if (user == null || user.getPersona() == null) return "-";
        return java.util.stream.Stream.of(user.getPersona().getNombre(), user.getPersona().getPrimerApellido(), user.getPersona().getSegundoApellido())
                .filter(java.util.Objects::nonNull).filter(value -> !value.isBlank()).reduce((a, b) -> a + " " + b).orElse("-");
    }

    public List<JustificacionRevisionResponse> pendingJustifications() {
        return justificaciones.findByEstadoOrderByCreadoEnDesc(EstadoJustificacion.PENDIENTE).stream()
                .map(j -> new JustificacionRevisionResponse(j.getId(), j.getAsistencia() == null ? null : j.getAsistencia().getId(),
                        j.getUsuario().getIdUser(), j.getComentario(), j.getNombreArchivo(), j.getCreadoEn(), j.getEstado())).toList();
    }

    public List<JustificacionAsistencia> myJustifications(String username) {
        return justificaciones.findByUsuarioIdUserOrderByCreadoEnDesc(usuario(username).getIdUser());
    }

    public UsuarioHorario mySchedule(String username) { return horarios.assigned(usuario(username).getIdUser()); }

    public TableroAsistenciaResponse dashboard(String username) {
        Usuario user = usuario(username);
        UsuarioHorario assignment = asignaciones.findFirstByUsuarioIdUserAndActivoTrueOrderByIdDesc(user.getIdUser()).orElse(null);
        BigDecimal hours = assignment == null ? BigDecimal.ZERO : horarios.weeklyHours(assignment.getHorario());
        BigDecimal hourly = user.getSueldoHora() != null ? user.getSueldoHora()
                : (hours.signum() == 0 || user.getSueldoSemanal() == null
                ? BigDecimal.ZERO : user.getSueldoSemanal().divide(hours, 2, java.math.RoundingMode.HALF_UP));
        List<AsistenciaTableroResponse> records = mine(username).stream()
                .map(item -> {
                    DiaSemana day = DiaSemana.values()[item.getFecha().getDayOfWeek().getValue() - 1];
                    HorarioDia scheduleDay = assignment == null ? null : assignment.getHorario().getDias().stream()
                            .filter(schedule -> schedule.getDia() == day).findFirst().orElse(null);
                    List<AsistenciaBreakResponse> breaks = scheduleDay == null ? List.of() : scheduleDay.getBreaks().stream()
                            .map(itemBreak -> new AsistenciaBreakResponse(itemBreak.getInicio(), itemBreak.getFin())).toList();
                    return AsistenciaTableroResponse.fromEntity(item,
                            marcajes.findFirstByAsistenciaIdOrderByMarcadoEnDesc(item.getId()), breaks);
                })
                .toList();
        return new TableroAsistenciaResponse(records, assignment, hours, hourly);
    }

    public Asistencia one(Integer id) { return asistencias.findById(id).orElseThrow(() -> new NotFoundException("Asistencia no encontrada")); }

    public JustificacionAsistencia justification(String username, Integer asistenciaId, String comentario, MultipartFile file) {
        Usuario user = usuario(username);
        Asistencia attendance = one(asistenciaId);
        if (!attendance.getUsuario().getIdUser().equals(user.getIdUser())) throw new IllegalArgumentException("Asistencia ajena");
        JustificacionAsistencia justification = justificaciones.save(JustificacionAsistencia.builder()
                .usuario(user).asistencia(attendance).comentario(comentario).estado(EstadoJustificacion.PENDIENTE)
                .creadoEn(LocalDateTime.now()).build());
        if (file != null && !file.isEmpty()) {
            try {
                Files.createDirectories(uploadDir);
                String filename = UUID.randomUUID() + "-justificacion";
                Path target = uploadDir.resolve(filename).normalize();
                file.transferTo(target);
                justification.setNombreArchivo(filename);
                justification.setRutaArchivo(target.toString());
                justificaciones.save(justification);
            } catch (IOException ex) { throw new IllegalStateException("No se pudo guardar el archivo", ex); }
        }
        return justification;
    }

    public Object reviewEvidence(Integer id, boolean approve) {
        EvidenciaAsistencia evidence = evidencias.findById(id).orElseThrow(() -> new NotFoundException("Evidencia no encontrada"));
        evidence.setEstado(approve ? EstadoEvidencia.APROBADA : EstadoEvidencia.RECHAZADA);
        Asistencia attendance = evidence.getMarcaje().getAsistencia();
        if (!approve) attendance.setEstado(EstadoIncidencia.RECHAZADA);
        else if (evidence.getMarcaje().getEstadoUbicacion() == EstadoIncidencia.APROBADA) attendance.setEstado(EstadoIncidencia.APROBADA);
        asistencias.save(attendance);
        return evidencias.save(evidence);
    }

    public Object reviewJustification(Integer id, boolean approve) {
        JustificacionAsistencia justification = justificaciones.findById(id).orElseThrow(() -> new NotFoundException("Justificacion no encontrada"));
        justification.setEstado(approve ? EstadoJustificacion.APROBADA : EstadoJustificacion.RECHAZADA);
        return justificaciones.save(justification);
    }

    @Transactional
    public List<DeduccionResponse> generateAttendanceDeductions(Integer usuarioId, LocalDate start, LocalDate end) {
        if (usuarioId == null || start == null || end == null || start.isAfter(end)) {
            throw new IllegalArgumentException("Usuario y rango de fechas validos son requeridos");
        }
        Usuario user = usuariosRepository.findById(usuarioId).orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        UsuarioHorario assignment = asignaciones.findFirstByUsuarioIdUserAndActivoTrueOrderByIdDesc(usuarioId)
                .orElseThrow(() -> new NotFoundException("Horario no asignado"));
        BigDecimal weeklyHours = horarios.weeklyHours(assignment.getHorario());
        BigDecimal weeklySalary = user.getSueldoSemanal();
        if (weeklySalary == null || weeklyHours.signum() == 0) throw new IllegalArgumentException("No se puede calcular sueldo por hora sin sueldo u horas semanales");
        BigDecimal hourlySalary = user.getSueldoHora() != null ? user.getSueldoHora()
                : weeklySalary.divide(weeklyHours, 2, java.math.RoundingMode.HALF_UP);
        ConfiguracionAsistencia settings = configuracion.get();
        List<DeduccionResponse> generated = new ArrayList<>();
        LocalDate lastEvaluableDate = end.isBefore(LocalDate.now()) ? end : LocalDate.now().minusDays(1);
        for (LocalDate date = start; !date.isAfter(lastEvaluableDate); date = date.plusDays(1)) {
            Optional<Asistencia> attendance = asistencias.findByUsuarioIdUserAndFecha(usuarioId, date);
            DiaSemana scheduledDay = DiaSemana.values()[date.getDayOfWeek().getValue() - 1];
            HorarioDia daySchedule = assignment.getHorario().getDias().stream().filter(day -> day.getDia() == scheduledDay).findFirst().orElse(null);
            if (daySchedule != null) {
                if (attendance.isEmpty()) {
                    createAbsenceDeduction(user, date, hourlySalary, scheduledMinutes(daySchedule), generated);
                } else if (attendance.get().getEstado() != EstadoIncidencia.RECHAZADA
                    && !justificaciones.existsByAsistenciaIdAndEstado(attendance.get().getId(), EstadoJustificacion.APROBADA)) {
                    Asistencia a = attendance.get();
                    if (a.getEntrada() != null) {
                        long lateMinutes = java.time.Duration.between(daySchedule.getEntrada().plusMinutes(settings.getToleranciaEntrada()), a.getEntrada().toLocalTime()).toMinutes();
                        if (lateMinutes > 0) createAttendanceDeduction(a, TipoDeduccion.RETARDO, hourlySalary, lateMinutes, generated);
                    }
                    if (a.getSalida() != null) {
                        long earlyMinutes = java.time.Duration.between(a.getSalida().toLocalTime(), daySchedule.getSalida().minusMinutes(settings.getToleranciaSalida())).toMinutes();
                        if (earlyMinutes > 0) createAttendanceDeduction(a, TipoDeduccion.SALIDA_TEMPRANA, hourlySalary, earlyMinutes, generated);
                    }
                }
            }
        }
        return generated;
    }

    private void createAttendanceDeduction(Asistencia attendance, TipoDeduccion type, BigDecimal hourlySalary, long minutes, List<DeduccionResponse> generated) {
        if (deduccionRepository.existsByAsistenciaIdAndTipo(attendance.getId(), type)) return;
        long deductedHours = Math.max(1, (minutes + 59) / 60);
        BigDecimal amount = hourlySalary.multiply(BigDecimal.valueOf(deductedHours));
        String concept = type == TipoDeduccion.RETARDO ? "Retardo (" + deductedHours + " h)" : "Salida temprana (" + deductedHours + " h)";
        Deduccion deduction = deduccionRepository.save(Deduccion.builder().usuario(attendance.getUsuario()).asistencia(attendance)
                .concepto(concept).monto(amount)
                .fecha(attendance.getFecha()).estado(EstadoDeduccion.PENDIENTE).tipo(type).origen(OrigenDeduccion.ASISTENCIA).build());
        generated.add(DeduccionResponse.fromEntity(deduction));
    }

    private void createAbsenceDeduction(Usuario user, LocalDate date, BigDecimal hourlySalary, long minutes, List<DeduccionResponse> generated) {
        if (deduccionRepository.existsByUsuarioIdUserAndFechaAndTipo(user.getIdUser(), date, TipoDeduccion.FALTA)) return;
        long scheduledHours = Math.max(1, (minutes + 59) / 60);
        BigDecimal amount = hourlySalary.multiply(BigDecimal.valueOf(scheduledHours));
        Deduccion deduction = deduccionRepository.save(Deduccion.builder().usuario(user)
                .concepto("Falta (" + scheduledHours + " h)").monto(amount).fecha(date)
                .estado(EstadoDeduccion.PENDIENTE).tipo(TipoDeduccion.FALTA).origen(OrigenDeduccion.ASISTENCIA).build());
        generated.add(DeduccionResponse.fromEntity(deduction));
    }

    private long scheduledMinutes(HorarioDia day) {
        long total = java.time.Duration.between(day.getEntrada(), day.getSalida()).toMinutes();
        return total - day.getBreaks().stream().mapToLong(item -> java.time.Duration.between(item.getInicio(), item.getFin()).toMinutes()).sum();
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double r = 6371000, p = Math.PI / 180;
        double a = .5 - Math.cos((lat2 - lat1) * p) / 2 + Math.cos(lat1 * p) * Math.cos(lat2 * p) * (1 - Math.cos((lon2 - lon1) * p)) / 2;
        return 2 * r * Math.asin(Math.sqrt(a));
    }
}
