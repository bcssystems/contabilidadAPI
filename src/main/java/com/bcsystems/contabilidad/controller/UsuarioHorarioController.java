package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.dto.AsignarHorarioRequest;
import com.bcsystems.contabilidad.service.HorarioService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/usuarios/{id}/horario")
public class UsuarioHorarioController {
    private final HorarioService service;

    public UsuarioHorarioController(HorarioService service) { this.service = service; }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Object get(@PathVariable Integer id) { return service.assigned(id); }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Object assign(@PathVariable Integer id, @RequestBody AsignarHorarioRequest request) {
        return service.assign(id, request);
    }
}
