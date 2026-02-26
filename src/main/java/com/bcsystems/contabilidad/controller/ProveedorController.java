package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.dto.ProveedorRegisterRequest;
import com.bcsystems.contabilidad.dto.ProveedorUpdateRequest;
import com.bcsystems.contabilidad.service.ProveedorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/proveedor")
public class ProveedorController {

    private final ProveedorService proveedorService;

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody ProveedorRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(proveedorService.save(request));
    }

    @GetMapping
    public ResponseEntity<?> findAll() {
        return ResponseEntity.status(HttpStatus.OK).body(proveedorService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(proveedorService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ProveedorUpdateRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(proveedorService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        proveedorService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
