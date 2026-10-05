package com.bcsystems.contabilidad.controller;
import com.bcsystems.contabilidad.dto.AsignarHorarioRequest;
import com.bcsystems.contabilidad.service.HorarioService;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController @RequestMapping("/api/v1/empleados/{id}/horario")
public class EmpleadoHorarioController {
    private final HorarioService service;
    public EmpleadoHorarioController(HorarioService service) { this.service = service; }
    @GetMapping @PreAuthorize("hasRole('ADMIN')") public Object get(@PathVariable Integer id) { return service.assigned(id); }
    @PostMapping @PreAuthorize("hasRole('ADMIN')") public Object assign(@PathVariable Integer id, @RequestBody AsignarHorarioRequest r) { return service.assign(id, r); }
}
