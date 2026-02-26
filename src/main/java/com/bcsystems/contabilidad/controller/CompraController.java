package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.dto.CompraRegisterRequest;
import com.bcsystems.contabilidad.dto.CompraUpdateRequest;
import com.bcsystems.contabilidad.service.CompraService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/compra")
public class CompraController {

    private final CompraService compraService;

    public CompraController(CompraService compraService) {
        this.compraService = compraService;
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody CompraRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(compraService.save(request));
    }

    @GetMapping
    public ResponseEntity<?> findAll() {
        return ResponseEntity.status(HttpStatus.OK).body(compraService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(compraService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody CompraUpdateRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(compraService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        compraService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
