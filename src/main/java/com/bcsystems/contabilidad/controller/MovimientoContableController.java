package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.dto.MovimientoContableRegisterRequest;
import com.bcsystems.contabilidad.dto.MovimientoContableUpdateRequest;
import com.bcsystems.contabilidad.service.MovimientoContableService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/movimiento-contable")
public class MovimientoContableController {

    private final MovimientoContableService movimientoContableService;

    public MovimientoContableController(MovimientoContableService movimientoContableService) {
        this.movimientoContableService = movimientoContableService;
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody MovimientoContableRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(movimientoContableService.save(request));
    }

    @GetMapping
    public ResponseEntity<?> findAll() {
        return ResponseEntity.status(HttpStatus.OK).body(movimientoContableService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(movimientoContableService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody MovimientoContableUpdateRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(movimientoContableService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        movimientoContableService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/asiento/{asientoId}")
    public ResponseEntity<?> findByAsiento(@PathVariable Long asientoId) {
        return ResponseEntity.status(HttpStatus.OK).body(movimientoContableService.findByAsiento(asientoId));
    }

    @GetMapping("/cuenta/{cuentaId}")
    public ResponseEntity<?> findByCuenta(@PathVariable Long cuentaId) {
        return ResponseEntity.status(HttpStatus.OK).body(movimientoContableService.findByCuenta(cuentaId));
    }
}
