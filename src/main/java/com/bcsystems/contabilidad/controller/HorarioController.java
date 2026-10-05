package com.bcsystems.contabilidad.controller;
import com.bcsystems.contabilidad.dto.*;
import com.bcsystems.contabilidad.service.HorarioService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController @RequestMapping("/api/v1/horarios")
@PreAuthorize("hasRole('ADMIN')")
public class HorarioController {
    private final HorarioService service;
    public HorarioController(HorarioService service) { this.service = service; }
    @GetMapping public Object all() { return service.findAll(); }
    @PostMapping public ResponseEntity<?> create(@RequestBody HorarioRequest r) { return ResponseEntity.status(HttpStatus.CREATED).body(service.save(r)); }
    @PutMapping("/{id}") public Object update(@PathVariable Integer id, @RequestBody HorarioRequest r) { return service.update(id, r); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Integer id) { service.delete(id); return ResponseEntity.noContent().build(); }
}
