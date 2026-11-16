package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.*;
import com.bcsystems.contabilidad.dto.*;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class MakerWorkspaceService {
    private static final Set<String> PROJECT_STATES = Set.of("COTIZACION", "MUESTREO", "PRODUCCION", "ENTREGA");
    private static final Set<String> TASK_STATES = Set.of("SIN_COMENZAR", "EN_PROGRESO", "HECHO");
    private static final Set<String> PRIORITIES = Set.of("BAJA", "MEDIA", "ALTA", "URGENTE");
    private final MakerProyectoRepository projectRepository;
    private final MakerTareaRepository taskRepository;
    private final MakerNotificacionRepository notificationRepository;
    private final MakerCotizacionRepository quoteRepository;
    private final UsuarioRepository userRepository;

    public MakerWorkspaceService(MakerProyectoRepository projectRepository, MakerTareaRepository taskRepository,
                                 MakerNotificacionRepository notificationRepository, MakerCotizacionRepository quoteRepository,
                                 UsuarioRepository userRepository) {
        this.projectRepository = projectRepository; this.taskRepository = taskRepository;
        this.notificationRepository = notificationRepository; this.quoteRepository = quoteRepository; this.userRepository = userRepository;
    }

    public List<MakerUserResponse> users() { return userRepository.findByActivoTrue().stream().map(u -> new MakerUserResponse(u.getIdUser(), u.getUsername())).toList(); }

    @Transactional(readOnly = true)
    public Page<MakerProyectoResponse> projects(Pageable pageable) { return projectRepository.findAllByOrderByFechaCreacionDesc(pageable).map(this::projectResponse); }

    @Transactional(readOnly = true)
    public MakerProyectoResponse project(Integer id) { return projectResponse(projectRepository.findById(id).orElseThrow(() -> new NotFoundException("Proyecto no encontrado"))); }

    @Transactional
    public MakerProyectoResponse saveProject(Integer id, MakerProyectoRequest request, String actor) {
        validate(request.estado(), request.prioridad(), PROJECT_STATES);
        MakerProyecto project = id == null ? MakerProyecto.builder().fechaCreacion(LocalDateTime.now()).build() : projectRepository.findById(id).orElseThrow(() -> new NotFoundException("Proyecto no encontrado"));
        project.setNombre(request.nombre().trim()); project.setDescripcion(request.descripcion()); project.setEstado(normalize(request.estado()));
        project.setPrioridad(normalize(request.prioridad())); project.setColor(request.color()); project.setFechaFin(request.fechaFin());
        if (request.cotizacionId() != null) project.setCotizacion(quoteRepository.findById(request.cotizacionId()).orElseThrow(() -> new NotFoundException("Cotización no encontrada")));
        else project.setCotizacion(null);
        project.getAsignados().clear(); project.getAsignados().addAll(resolveUsers(request.usuarioIds()));
        MakerProyecto saved = projectRepository.save(project);
        if (saved.getCotizacion() != null) { saved.getCotizacion().setProyecto(saved); quoteRepository.save(saved.getCotizacion()); }
        notifyUsers(saved.getAsignados(), "Proyecto asignado", "Fuiste asignado al proyecto: " + saved.getNombre(), "PROYECTO", saved.getId(), actor);
        return projectResponse(saved);
    }

    @Transactional
    public void deleteProject(Integer id) { if (!projectRepository.existsById(id)) throw new NotFoundException("Proyecto no encontrado"); taskRepository.findByProyectoIdOrderByFechaCreacionDesc(id).forEach(t -> { t.setProyecto(null); taskRepository.save(t); }); projectRepository.deleteById(id); }

    @Transactional(readOnly = true)
    public Page<MakerTareaResponse> tasks(Pageable pageable) { return taskRepository.findAllByOrderByFechaCreacionDesc(pageable).map(this::taskResponse); }

    @Transactional(readOnly = true)
    public List<MakerTareaResponse> projectTasks(Integer projectId) { return taskRepository.findByProyectoIdOrderByFechaCreacionDesc(projectId).stream().map(this::taskResponse).toList(); }

    @Transactional(readOnly = true)
    public MakerTareaResponse task(Integer id) { return taskResponse(taskRepository.findById(id).orElseThrow(() -> new NotFoundException("Tarea no encontrada"))); }

    @Transactional
    public MakerTareaResponse saveTask(Integer id, MakerTareaRequest request, String actor) {
        validate(request.estado(), request.prioridad(), TASK_STATES);
        MakerTarea task = id == null ? MakerTarea.builder().fechaCreacion(LocalDateTime.now()).build() : taskRepository.findById(id).orElseThrow(() -> new NotFoundException("Tarea no encontrada"));
        task.setNombre(request.nombre().trim()); task.setDescripcion(request.descripcion()); task.setChecklist(request.checklist());
        task.setEstado(normalize(request.estado())); task.setPrioridad(normalize(request.prioridad())); task.setColor(request.color()); task.setFechaFin(request.fechaFin());
        task.setProyecto(request.proyectoId() == null ? null : projectRepository.findById(request.proyectoId()).orElseThrow(() -> new NotFoundException("Proyecto no encontrado")));
        task.getAsignados().clear(); task.getAsignados().addAll(resolveUsers(request.usuarioIds()));
        task.getTareasMencionadas().clear();
        if (request.tareasMencionadasIds() != null) task.getTareasMencionadas().addAll(taskRepository.findAllById(request.tareasMencionadasIds()));
        MakerTarea saved = taskRepository.save(task);
        notifyUsers(saved.getAsignados(), "Tarea asignada", "Fuiste asignado a la tarea: " + saved.getNombre(), "TAREA", saved.getId(), actor);
        if (request.usuariosMencionadosIds() != null) notifyUsers(resolveUsers(request.usuariosMencionadosIds()), "Te mencionaron", "Fuiste mencionado en la tarea: " + saved.getNombre(), "TAREA", saved.getId(), actor);
        for (MakerTarea mentioned : saved.getTareasMencionadas()) notifyUsers(mentioned.getAsignados(), "Tarea relacionada", "La tarea " + saved.getNombre() + " mencionó tu tarea: " + mentioned.getNombre(), "TAREA", saved.getId(), actor);
        return taskResponse(saved);
    }

    @Transactional
    public void deleteTask(Integer id) { if (!taskRepository.existsById(id)) throw new NotFoundException("Tarea no encontrada"); taskRepository.deleteById(id); }

    @Transactional(readOnly = true)
    public Page<MakerNotificacionResponse> notifications(String username, Pageable pageable) {
        Integer userId = userRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("Usuario no encontrado")).getIdUser();
        return notificationRepository.findByUsuarioIdUserOrderByFechaDesc(userId, pageable).map(this::notificationResponse);
    }

    @Transactional(readOnly = true)
    public long unreadNotifications(String username) { return notificationRepository.countByUsuarioIdUserAndLeidaFalse(userRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("Usuario no encontrado")).getIdUser()); }

    @Transactional
    public void readNotification(Integer id, String username) { MakerNotificacion n = notificationRepository.findById(id).orElseThrow(() -> new NotFoundException("Notificación no encontrada")); checkOwner(n, username); n.setLeida(true); notificationRepository.save(n); }

    @Transactional
    public void readAllNotifications(String username) { notificationRepository.markAllRead(userRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("Usuario no encontrado")).getIdUser()); }

    private void checkOwner(MakerNotificacion n, String username) { if (!n.getUsuario().getUsername().equals(username)) throw new IllegalArgumentException("Notificación no pertenece al usuario"); }
    private void validate(String state, String priority, Set<String> validStates) { if (!validStates.contains(normalize(state))) throw new IllegalArgumentException("Estado no válido"); if (!PRIORITIES.contains(normalize(priority))) throw new IllegalArgumentException("Prioridad no válida"); }
    private String normalize(String value) { return value == null ? "" : value.trim().toUpperCase(Locale.ROOT).replace(' ', '_'); }
    private List<Usuario> resolveUsers(List<Integer> ids) { return ids == null ? new ArrayList<>() : userRepository.findAllById(ids); }

    private void notifyUsers(List<Usuario> users, String title, String message, String referenceType, Integer referenceId, String actor) {
        Set<Integer> unique = new HashSet<>();
        for (Usuario user : users) if (user != null && unique.add(user.getIdUser())) notificationRepository.save(MakerNotificacion.builder().usuario(user).titulo(title).mensaje(message).tipo("MAKER").referenciaTipo(referenceType).referenciaId(referenceId).leida(false).fecha(LocalDateTime.now()).build());
    }
    private MakerUserResponse userResponse(Usuario u) { return new MakerUserResponse(u.getIdUser(), u.getUsername()); }
    private MakerProyectoResponse projectResponse(MakerProyecto p) { long total = taskRepository.countByProyectoId(p.getId()); long done = taskRepository.countByProyectoIdAndEstado(p.getId(), "HECHO"); return new MakerProyectoResponse(p.getId(), p.getNombre(), p.getDescripcion(), p.getEstado(), p.getPrioridad(), p.getColor(), p.getFechaFin(), p.getFechaCreacion(), p.getCotizacion() == null ? null : p.getCotizacion().getId(), p.getCotizacion() == null ? null : p.getCotizacion().getFolio(), p.getAsignados().stream().map(this::userResponse).toList(), total, done); }
    private MakerTareaResponse taskResponse(MakerTarea t) { return new MakerTareaResponse(t.getId(), t.getNombre(), t.getDescripcion(), t.getChecklist(), t.getEstado(), t.getPrioridad(), t.getColor(), t.getFechaFin(), t.getFechaCreacion(), t.getProyecto() == null ? null : t.getProyecto().getId(), t.getProyecto() == null ? null : t.getProyecto().getNombre(), t.getAsignados().stream().map(this::userResponse).toList(), t.getTareasMencionadas().stream().map(x -> new MakerTareaResponse.MakerTareaMentionResponse(x.getId(), x.getNombre())).toList()); }
    private MakerNotificacionResponse notificationResponse(MakerNotificacion n) { return new MakerNotificacionResponse(n.getId(), n.getTitulo(), n.getMensaje(), n.getTipo(), n.getReferenciaTipo(), n.getReferenciaId(), n.getLeida(), n.getFecha()); }
}
