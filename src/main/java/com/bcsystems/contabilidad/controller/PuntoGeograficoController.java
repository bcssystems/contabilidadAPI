package com.bcsystems.contabilidad.controller;
import com.bcsystems.contabilidad.dto.PuntoGeograficoRequest;
import com.bcsystems.contabilidad.service.PuntoGeograficoService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/puntos-geograficos")
public class PuntoGeograficoController {
    private final PuntoGeograficoService service;
    public PuntoGeograficoController(PuntoGeograficoService service) { this.service = service; }
    @GetMapping public Object all() { return service.all(); }
    @PostMapping public ResponseEntity<?> create(@RequestBody PuntoGeograficoRequest r) { return ResponseEntity.status(HttpStatus.CREATED).body(service.save(r)); }
    @PutMapping("/{id}") public Object update(@PathVariable Integer id, @RequestBody PuntoGeograficoRequest r) { return service.update(id, r); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Integer id) { service.delete(id); return ResponseEntity.noContent().build(); }
}
