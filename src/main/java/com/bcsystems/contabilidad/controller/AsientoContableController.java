package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.dto.AsientoContableRegisterRequest;
import com.bcsystems.contabilidad.dto.AsientoContableUpdateRequest;
import com.bcsystems.contabilidad.service.AsientoContableService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/asiento-contable")
public class AsientoContableController {

    private final AsientoContableService asientoContableService;

    public AsientoContableController(AsientoContableService asientoContableService) {
        this.asientoContableService = asientoContableService;
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody AsientoContableRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(asientoContableService.save(request));
    }

    @GetMapping
    public ResponseEntity<?> findAll() {
        return ResponseEntity.status(HttpStatus.OK).body(asientoContableService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(asientoContableService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody AsientoContableUpdateRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(asientoContableService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        asientoContableService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
