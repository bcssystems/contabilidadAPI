package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.dto.DeduccionFijaRequest;
import com.bcsystems.contabilidad.service.DeduccionFijaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/deduccion-fija")
public class DeduccionFijaController {
    private final DeduccionFijaService service;

    public DeduccionFijaController(DeduccionFijaService service) { this.service = service; }

    @GetMapping
    public Object all() { return service.findAll(); }

    @GetMapping("/{id}")
    public Object one(@PathVariable Long id) { return service.findById(id); }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody DeduccionFijaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(request));
    }

    @PutMapping("/{id}")
    public Object update(@PathVariable Long id, @RequestBody DeduccionFijaRequest request) { return service.update(id, request); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.noContent().build(); }
}
