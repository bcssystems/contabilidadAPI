package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.dto.GastoRegisterRequest;
import com.bcsystems.contabilidad.dto.GastoUpdateRequest;
import com.bcsystems.contabilidad.service.GastoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gasto")
public class GastoController {

    private final GastoService gastoService;

    public GastoController(GastoService gastoService) {
        this.gastoService = gastoService;
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody GastoRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(gastoService.save(request));
    }

    @GetMapping
    public ResponseEntity<?> findAll() {
        return ResponseEntity.status(HttpStatus.OK).body(gastoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(gastoService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody GastoUpdateRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(gastoService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        gastoService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
