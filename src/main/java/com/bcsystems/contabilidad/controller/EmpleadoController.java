package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.dto.EmpleadoRegisterRequest;
import com.bcsystems.contabilidad.dto.EmpleadoResponse;
import com.bcsystems.contabilidad.dto.EmpleadoUpdateRequest;
import com.bcsystems.contabilidad.service.EmpleadoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/empleado")
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
    public ResponseEntity<?> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "idEmpleado") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<EmpleadoResponse> result = empleadoService.findAllPageable(pageRequest);
        
        return ResponseEntity.status(HttpStatus.OK).body(result);
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
