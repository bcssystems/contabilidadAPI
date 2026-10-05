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
    private final UsuarioService usuarios; private final EmpleadoRepository empleados; private final AsistenciaRepository asistencias;
    private final MarcajeRepository marcajes; private final EvidenciaAsistenciaRepository evidencias; private final PuntoGeograficoRepository puntos;
    private final JustificacionAsistenciaRepository justificaciones; private final DeduccionRepository deduccionRepository; private final Path uploadDir;
    private final ConfiguracionAsistenciaService configuracion; private final HorarioService horarios;
    private final EmpleadoHorarioRepository asignaciones;
    public AsistenciaService(UsuarioService usuarios, EmpleadoRepository empleados, AsistenciaRepository asistencias, MarcajeRepository marcajes,
                              EvidenciaAsistenciaRepository evidencias, PuntoGeograficoRepository puntos, JustificacionAsistenciaRepository justificaciones,
                              ConfiguracionAsistenciaService configuracion, HorarioService horarios,
                               EmpleadoHorarioRepository asignaciones, DeduccionRepository deduccionRepository,
                               @Value("${app.attendance.upload.dir:./uploads}") String uploadDir) {
        this.usuarios=usuarios; this.empleados=empleados; this.asistencias=asistencias; this.marcajes=marcajes; this.evidencias=evidencias; this.puntos=puntos; this.justificaciones=justificaciones; this.deduccionRepository=deduccionRepository; this.configuracion=configuracion; this.horarios=horarios; this.asignaciones=asignaciones; this.uploadDir=Paths.get(uploadDir).toAbsolutePath().normalize().resolve("asistencias");
    }
    private Empleado employee(String username) { Usuario u=usuarios.findByUsername(username).orElseThrow(() -> new NotFoundException("Usuario no encontrado")); return empleados.findByUsuarioIdUser(u.getIdUser()).orElseThrow(() -> new NotFoundException("Empleado no encontrado")); }
    @Transactional public MarcajeResponse mark(String username, TipoMarcaje type, MultipartFile selfie, BigDecimal lat, BigDecimal lon, BigDecimal precision) {
        if (configuracion.get().getRequiereSelfie() && (selfie == null || selfie.isEmpty())) throw new IllegalArgumentException("La selfie es obligatoria");
        if (lat == null || lon == null) throw new IllegalArgumentException("La ubicacion es obligatoria");
        Empleado e=employee(username); LocalDateTime now=LocalDateTime.now(java.time.Clock.systemUTC()); LocalDate date=now.toLocalDate();
        Asistencia a=asistencias.findByEmpleadoIdEmpleadoAndFecha(e.getIdEmpleado(), date).orElseGet(() -> asistencias.save(Asistencia.builder().empleado(e).fecha(date).estado(EstadoIncidencia.PENDIENTE).build()));
        if (marcajes.existsByAsistenciaIdAndTipo(a.getId(), type)) throw new IllegalArgumentException("El marcaje ya existe para hoy");
        if (type == TipoMarcaje.ENTRADA) a.setEntrada(now); else a.setSalida(now);
        PuntoGeografico closest=null; double distance=Double.MAX_VALUE;
        for (PuntoGeografico p:puntos.findByActivoTrue()) { double d=distance(lat.doubleValue(),lon.doubleValue(),p.getLatitud().doubleValue(),p.getLongitud().doubleValue()); if(d<distance){distance=d;closest=p;} }
        int configuredRadius = configuracion.get().getRadioMaximo();
        boolean inside=closest != null && distance <= Math.min(closest.getRadioMetros(), configuredRadius);
        Marcaje m=marcajes.save(Marcaje.builder().asistencia(a).tipo(type).marcadoEn(now).latitud(lat).longitud(lon).precisionMetros(precision).distanciaMetros(closest == null ? null : distance).estadoUbicacion(inside ? EstadoIncidencia.APROBADA : EstadoIncidencia.PENDIENTE).build());
        if (selfie != null && !selfie.isEmpty()) try { Files.createDirectories(uploadDir); String original=selfie.getOriginalFilename() == null ? "selfie" : selfie.getOriginalFilename(); String ext=original.contains(".") ? original.substring(original.lastIndexOf('.')).replaceAll("[^a-zA-Z0-9.]", "") : ".jpg"; String filename=UUID.randomUUID()+ext; Path target=uploadDir.resolve(filename).normalize(); if(!target.getParent().equals(uploadDir.toAbsolutePath().normalize())) throw new IOException("Ruta invalida"); selfie.transferTo(target); evidencias.save(EvidenciaAsistencia.builder().marcaje(m).nombreArchivo(filename).rutaArchivo(target.toString()).creadoEn(now).estado(EstadoEvidencia.PENDIENTE).build()); } catch(IOException ex) { throw new IllegalStateException("No se pudo guardar la selfie", ex); }
        a.setEstado(inside ? EstadoIncidencia.APROBADA : EstadoIncidencia.PENDIENTE);
        asistencias.save(a); return new MarcajeResponse(a.getId(),m.getId(),type,now,a.getEntrada(),a.getSalida(),m.getDistanciaMetros(),m.getEstadoUbicacion().name(),a.getEstado().name(),!inside);
    }
    public List<Asistencia> mine(String username) { return asistencias.findByEmpleadoIdEmpleadoOrderByFechaDesc(employee(username).getIdEmpleado()); }
    public List<Asistencia> all() { return asistencias.findAll(); }
    public List<EvidenciaRevisionResponse> pendingEvidence() { return evidencias.findByEstadoOrderByCreadoEnDesc(EstadoEvidencia.PENDIENTE).stream().map(e -> new EvidenciaRevisionResponse(e.getId(), e.getMarcaje().getId(), e.getMarcaje().getAsistencia().getId(), e.getMarcaje().getAsistencia().getEmpleado().getIdEmpleado(), e.getMarcaje().getTipo().name(), e.getMarcaje().getMarcadoEn(), e.getMarcaje().getDistanciaMetros(), e.getEstado())).toList(); }
    public List<JustificacionRevisionResponse> pendingJustifications() { return justificaciones.findByEstadoOrderByCreadoEnDesc(EstadoJustificacion.PENDIENTE).stream().map(j -> new JustificacionRevisionResponse(j.getId(), j.getAsistencia() == null ? null : j.getAsistencia().getId(), j.getEmpleado().getIdEmpleado(), j.getComentario(), j.getNombreArchivo(), j.getCreadoEn(), j.getEstado())).toList(); }
    public List<JustificacionAsistencia> myJustifications(String username) { return justificaciones.findByEmpleadoIdEmpleadoOrderByCreadoEnDesc(employee(username).getIdEmpleado()); }
    public EmpleadoHorario mySchedule(String username) { return horarios.assigned(employee(username).getIdEmpleado()); }
    public TableroAsistenciaResponse dashboard(String username) { Empleado e = employee(username); EmpleadoHorario assignment = asignaciones.findFirstByEmpleadoIdEmpleadoAndActivoTrueOrderByIdDesc(e.getIdEmpleado()).orElse(null); BigDecimal hours = assignment == null ? BigDecimal.ZERO : horarios.weeklyHours(assignment.getHorario()); BigDecimal hourly = hours.signum() == 0 || e.getSueldoSemanal() == null ? BigDecimal.ZERO : e.getSueldoSemanal().divide(hours, 2, java.math.RoundingMode.HALF_UP); return new TableroAsistenciaResponse(mine(username), assignment, hours, hourly); }
    public Asistencia one(Integer id) { return asistencias.findById(id).orElseThrow(() -> new NotFoundException("Asistencia no encontrada")); }
    public JustificacionAsistencia justification(String username, Integer asistenciaId, String comentario, MultipartFile file) {
        Empleado e=employee(username); Asistencia a=one(asistenciaId); if(!a.getEmpleado().getIdEmpleado().equals(e.getIdEmpleado())) throw new IllegalArgumentException("Asistencia ajena");
        JustificacionAsistencia j=justificaciones.save(JustificacionAsistencia.builder().empleado(e).asistencia(a).comentario(comentario).estado(EstadoJustificacion.PENDIENTE).creadoEn(LocalDateTime.now()).build());
        if(file != null && !file.isEmpty()) { try { Files.createDirectories(uploadDir); String filename=UUID.randomUUID()+"-justificacion"; Path target=uploadDir.resolve(filename).normalize(); file.transferTo(target); j.setNombreArchivo(filename); j.setRutaArchivo(target.toString()); justificaciones.save(j); } catch(IOException ex) { throw new IllegalStateException("No se pudo guardar el archivo", ex); } }
        return j;
    }
    public Object reviewEvidence(Integer id, boolean approve) { EvidenciaAsistencia e=evidencias.findById(id).orElseThrow(() -> new NotFoundException("Evidencia no encontrada")); e.setEstado(approve?EstadoEvidencia.APROBADA:EstadoEvidencia.RECHAZADA); return evidencias.save(e); }
    public Object reviewJustification(Integer id, boolean approve) { JustificacionAsistencia j=justificaciones.findById(id).orElseThrow(() -> new NotFoundException("Justificacion no encontrada")); j.setEstado(approve?EstadoJustificacion.APROBADA:EstadoJustificacion.RECHAZADA); return justificaciones.save(j); }
    @Transactional
    public List<DeduccionResponse> generateAttendanceDeductions(Integer employeeId, LocalDate start, LocalDate end) {
        if (employeeId == null || start == null || end == null || start.isAfter(end)) {
            throw new IllegalArgumentException("Empleado y rango de fechas validos son requeridos");
        }
        Empleado employee = empleados.findByIdEmpleado(employeeId)
                .orElseThrow(() -> new NotFoundException("Empleado no encontrado"));
        EmpleadoHorario assignment = asignaciones.findFirstByEmpleadoIdEmpleadoAndActivoTrueOrderByIdDesc(employeeId)
                .orElseThrow(() -> new NotFoundException("Horario no asignado"));
        BigDecimal weeklyHours = horarios.weeklyHours(assignment.getHorario());
        BigDecimal weeklySalary = employee.getSueldoSemanal() != null ? employee.getSueldoSemanal() : employee.getSueldoBase();
        if (weeklySalary == null || weeklyHours.signum() == 0) {
            throw new IllegalArgumentException("No se puede calcular sueldo por hora sin sueldo u horas semanales");
        }
        BigDecimal hourlySalary = weeklySalary.divide(weeklyHours, 2, java.math.RoundingMode.HALF_UP);
        List<DeduccionResponse> generated = new ArrayList<>();
        LocalDate date = start;
        while (!date.isAfter(end)) {
            Optional<Asistencia> attendance = asistencias.findByEmpleadoIdEmpleadoAndFecha(employeeId, date);
            if (attendance.isPresent() && attendance.get().getEstado() == EstadoIncidencia.APROBADA
                    && !justificaciones.existsByAsistenciaIdAndEstado(attendance.get().getId(), EstadoJustificacion.APROBADA)) {
                DiaSemana scheduledDay = DiaSemana.values()[date.getDayOfWeek().getValue() - 1];
                HorarioDia daySchedule = assignment.getHorario().getDias().stream()
                        .filter(day -> day.getDia() == scheduledDay).findFirst().orElse(null);
                if (daySchedule != null) {
                    Asistencia a = attendance.get();
                    if (a.getEntrada() != null && a.getEntrada().toLocalTime().isAfter(daySchedule.getEntrada().plusMinutes(assignment.getHorario().getToleranciaEntradaMinutos()))) {
                        createAttendanceDeduction(a, TipoDeduccion.RETARDO, hourlySalary, generated);
                    }
                    if (a.getSalida() != null && a.getSalida().toLocalTime().isBefore(daySchedule.getSalida().minusMinutes(assignment.getHorario().getToleranciaSalidaMinutos()))) {
                        createAttendanceDeduction(a, TipoDeduccion.SALIDA_TEMPRANA, hourlySalary, generated);
                    }
                }
            }
            date = date.plusDays(1);
        }
        return generated;
    }
    private void createAttendanceDeduction(Asistencia attendance, TipoDeduccion type, BigDecimal amount, List<DeduccionResponse> generated) {
        if (deduccionRepository.existsByAsistenciaIdAndTipo(attendance.getId(), type)) return;
        Deduccion deduction = deduccionRepository.save(Deduccion.builder().empleado(attendance.getEmpleado()).asistencia(attendance)
                .concepto(type == TipoDeduccion.RETARDO ? "Retardo" : "Salida temprana").monto(amount)
                .fecha(attendance.getFecha()).estado(EstadoDeduccion.PENDIENTE).tipo(type).origen(OrigenDeduccion.ASISTENCIA).build());
        generated.add(DeduccionResponse.fromEntity(deduction));
    }
    private double distance(double lat1,double lon1,double lat2,double lon2){ double r=6371000, p=Math.PI/180, a=.5-Math.cos((lat2-lat1)*p)/2+Math.cos(lat1*p)*Math.cos(lat2*p)*(1-Math.cos((lon2-lon1)*p))/2; return 2*r*Math.asin(Math.sqrt(a)); }
}
