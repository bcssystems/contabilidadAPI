package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.dto.SucursalRegisterRequest;
import com.bcsystems.contabilidad.dto.SucursalResponse;
import com.bcsystems.contabilidad.dto.SucursalUpdateRequest;
import com.bcsystems.contabilidad.service.SucursalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sucursales")
public class SucursalController {

    private final SucursalService sucursalService;

    public SucursalController(SucursalService sucursalService) {
        this.sucursalService = sucursalService;
    }

    @GetMapping
    public ResponseEntity<List<SucursalResponse>> listarTodas() {
        return ResponseEntity.ok(sucursalService.listarTodas());
    }

    @GetMapping("/activas")
    public ResponseEntity<List<SucursalResponse>> listarActivas() {
        return ResponseEntity.ok(sucursalService.listarActivas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SucursalResponse> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(sucursalService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<SucursalResponse> crear(@Valid @RequestBody SucursalRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sucursalService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SucursalResponse> actualizar(@PathVariable Integer id, @Valid @RequestBody SucursalUpdateRequest request) {
        return ResponseEntity.ok(sucursalService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        sucursalService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
