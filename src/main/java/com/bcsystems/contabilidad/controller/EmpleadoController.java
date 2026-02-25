package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.dto.EmpleadoRegisterRequest;
import com.bcsystems.contabilidad.dto.EmpleadoUpdateRequest;
import com.bcsystems.contabilidad.service.EmpleadoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/empleado")
public class EmpleadoController {

    private final EmpleadoService empleadoService;

    public EmpleadoController(EmpleadoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody EmpleadoRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(empleadoService.save(request));
    }

    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<?> findByNombre(@PathVariable String nombre) {
        return ResponseEntity.status(HttpStatus.OK).body(empleadoService.findByNombre(nombre));
    }

    @GetMapping
    public ResponseEntity<?> findAll() {
        return ResponseEntity.status(HttpStatus.OK).body(empleadoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Integer id) {
        return ResponseEntity.status(HttpStatus.OK).body(empleadoService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody EmpleadoUpdateRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(empleadoService.updateEmpleado(id, request));
    }
}
