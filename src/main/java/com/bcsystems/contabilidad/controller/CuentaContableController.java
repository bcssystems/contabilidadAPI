package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.dto.CuentaContableRegisterRequest;
import com.bcsystems.contabilidad.dto.CuentaContableUpdateRequest;
import com.bcsystems.contabilidad.service.CuentaContableService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cuenta-contable")
public class CuentaContableController {

    private final CuentaContableService cuentaContableService;

    public CuentaContableController(CuentaContableService cuentaContableService) {
        this.cuentaContableService = cuentaContableService;
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody CuentaContableRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(cuentaContableService.save(request));
    }

    @GetMapping
    public ResponseEntity<?> findAll() {
        return ResponseEntity.status(HttpStatus.OK).body(cuentaContableService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(cuentaContableService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody CuentaContableUpdateRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(cuentaContableService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        cuentaContableService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
