package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.dto.AtributoRegisterRequest;
import com.bcsystems.contabilidad.dto.AtributoResponse;
import com.bcsystems.contabilidad.dto.AtributoUpdateRequest;
import com.bcsystems.contabilidad.service.AtributoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/atributos")
public class AtributoController {

    private final AtributoService atributoService;

    public AtributoController(AtributoService atributoService) {
        this.atributoService = atributoService;
    }

    @GetMapping
    public ResponseEntity<List<AtributoResponse>> listarTodas() {
        return ResponseEntity.ok(atributoService.listarTodas());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<AtributoResponse>> listarActivas() {
        return ResponseEntity.ok(atributoService.listarActivas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AtributoResponse> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(atributoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<AtributoResponse> crear(@Valid @RequestBody AtributoRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(atributoService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AtributoResponse> actualizar(@PathVariable Integer id, @Valid @RequestBody AtributoUpdateRequest request) {
        return ResponseEntity.ok(atributoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        atributoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
