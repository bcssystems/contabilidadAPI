package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.dto.VentaRegisterRequest;
import com.bcsystems.contabilidad.dto.VentaUpdateRequest;
import com.bcsystems.contabilidad.service.VentaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/venta")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody VentaRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(ventaService.save(request));
    }

    @GetMapping
    public ResponseEntity<?> findAll() {
        return ResponseEntity.status(HttpStatus.OK).body(ventaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(ventaService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody VentaUpdateRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(ventaService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        ventaService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
