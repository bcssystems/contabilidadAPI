package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.domain.MakerConfiguracion;
import com.bcsystems.contabilidad.dto.*;
import com.bcsystems.contabilidad.service.MakerService;
import com.bcsystems.contabilidad.service.MakerWorkspaceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/maker")
public class MakerController {
    private final MakerService service;
    public MakerController(MakerService service, MakerWorkspaceService workspace) { this.service = service; this.workspace = workspace; }

    @GetMapping("/materiales")
    public ResponseEntity<?> materiales(@RequestParam(defaultValue = "false") boolean todos) { return ResponseEntity.ok(service.materials(todos)); }

    @PostMapping("/materiales")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crearMaterial(@Valid @RequestBody MakerMaterialRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(service.saveMaterial(null, request)); }

    @PutMapping("/materiales/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizarMaterial(@PathVariable Integer id, @Valid @RequestBody MakerMaterialRequest request) { return ResponseEntity.ok(service.saveMaterial(id, request)); }

    @DeleteMapping("/materiales/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> desactivarMaterial(@PathVariable Integer id) { service.deleteMaterial(id); return ResponseEntity.noContent().build(); }

    @GetMapping("/filamentos")
    public ResponseEntity<?> filamentos(@RequestParam(defaultValue = "false") boolean todos) { return ResponseEntity.ok(service.filaments(todos)); }

    @PostMapping("/filamentos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crearFilamento(@Valid @RequestBody MakerFilamentoRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(service.saveFilament(null, request)); }

    @PutMapping("/filamentos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizarFilamento(@PathVariable Integer id, @Valid @RequestBody MakerFilamentoRequest request) { return ResponseEntity.ok(service.saveFilament(id, request)); }

    @DeleteMapping("/filamentos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> desactivarFilamento(@PathVariable Integer id) { service.deleteFilament(id); return ResponseEntity.noContent().build(); }

    @GetMapping("/configuracion")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MakerConfiguracion>> configuracion() { return ResponseEntity.ok(service.settings()); }

    @GetMapping("/configuracion/publica")
    public ResponseEntity<List<MakerConfiguracion>> configuracionPublica() { return ResponseEntity.ok(service.settings()); }

    @PutMapping("/configuracion/{clave}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizarConfiguracion(@PathVariable String clave, @Valid @RequestBody MakerConfiguracionRequest request) { return ResponseEntity.ok(service.updateSetting(clave, request)); }

    @PostMapping("/cotizaciones")
    public ResponseEntity<MakerResponse> crearCotizacion(@Valid @RequestBody MakerCotizacionRequest request, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createQuote(request, principal == null ? null : principal.getName()));
    }

    @GetMapping("/cotizaciones")
    public ResponseEntity<?> cotizaciones(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.quotes(PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100), Sort.by("fecha").descending())));
    }

    @GetMapping("/cotizaciones/{id}")
    public ResponseEntity<MakerResponse> cotizacion(@PathVariable Integer id) { return ResponseEntity.ok(service.quote(id)); }

    @DeleteMapping("/cotizaciones/{id}")
    public ResponseEntity<?> eliminarCotizacion(@PathVariable Integer id) { service.deleteQuote(id); return ResponseEntity.noContent().build(); }

    @PatchMapping("/cotizaciones/{id}/precio-final")
    public ResponseEntity<MakerResponse> actualizarPrecioFinal(@PathVariable Integer id, @Valid @RequestBody MakerPrecioFinalRequest request) { return ResponseEntity.ok(service.updateFinalPrice(id, request)); }

    @GetMapping("/usuarios")
    public ResponseEntity<List<MakerUserResponse>> usuarios() { return ResponseEntity.ok(workspace.users()); }

    @GetMapping("/proyectos")
    public ResponseEntity<?> proyectos(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "100") int size) {
        return ResponseEntity.ok(workspace.projects(PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 200))));
    }

    @GetMapping("/proyectos/{id}")
    public ResponseEntity<MakerProyectoResponse> proyecto(@PathVariable Integer id) { return ResponseEntity.ok(workspace.project(id)); }

    @PostMapping("/proyectos")
    public ResponseEntity<MakerProyectoResponse> crearProyecto(@Valid @RequestBody MakerProyectoRequest request, Principal principal) { return ResponseEntity.status(HttpStatus.CREATED).body(workspace.saveProject(null, request, principal == null ? null : principal.getName())); }

    @PutMapping("/proyectos/{id}")
    public ResponseEntity<MakerProyectoResponse> actualizarProyecto(@PathVariable Integer id, @Valid @RequestBody MakerProyectoRequest request, Principal principal) { return ResponseEntity.ok(workspace.saveProject(id, request, principal == null ? null : principal.getName())); }

    @DeleteMapping("/proyectos/{id}")
    public ResponseEntity<?> eliminarProyecto(@PathVariable Integer id) { workspace.deleteProject(id); return ResponseEntity.noContent().build(); }

    @GetMapping("/proyectos/{id}/tareas")
    public ResponseEntity<List<MakerTareaResponse>> tareasProyecto(@PathVariable Integer id) { return ResponseEntity.ok(workspace.projectTasks(id)); }

    @GetMapping("/tareas")
    public ResponseEntity<?> tareas(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "100") int size) { return ResponseEntity.ok(workspace.tasks(PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 200)))); }

    @GetMapping("/tareas/{id}")
    public ResponseEntity<MakerTareaResponse> tarea(@PathVariable Integer id) { return ResponseEntity.ok(workspace.task(id)); }

    @PostMapping("/tareas")
    public ResponseEntity<MakerTareaResponse> crearTarea(@Valid @RequestBody MakerTareaRequest request, Principal principal) { return ResponseEntity.status(HttpStatus.CREATED).body(workspace.saveTask(null, request, principal == null ? null : principal.getName())); }

    @PutMapping("/tareas/{id}")
    public ResponseEntity<MakerTareaResponse> actualizarTarea(@PathVariable Integer id, @Valid @RequestBody MakerTareaRequest request, Principal principal) { return ResponseEntity.ok(workspace.saveTask(id, request, principal == null ? null : principal.getName())); }

    @DeleteMapping("/tareas/{id}")
    public ResponseEntity<?> eliminarTarea(@PathVariable Integer id) { workspace.deleteTask(id); return ResponseEntity.noContent().build(); }

    @GetMapping("/notificaciones")
    public ResponseEntity<?> notificaciones(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, Principal principal) { return ResponseEntity.ok(workspace.notifications(principal.getName(), PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100)))); }

    @GetMapping("/notificaciones/no-leidas")
    public ResponseEntity<Long> notificacionesNoLeidas(Principal principal) { return ResponseEntity.ok(workspace.unreadNotifications(principal.getName())); }

    @PatchMapping("/notificaciones/{id}/leida")
    public ResponseEntity<?> marcarNotificacion(@PathVariable Integer id, Principal principal) { workspace.readNotification(id, principal.getName()); return ResponseEntity.noContent().build(); }

    @PatchMapping("/notificaciones/leer-todas")
    public ResponseEntity<?> marcarTodas(Principal principal) { workspace.readAllNotifications(principal.getName()); return ResponseEntity.noContent().build(); }

    private final MakerWorkspaceService workspace;
}
