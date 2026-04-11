package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.dto.MovimientoContableRegisterRequest;
import com.bcsystems.contabilidad.dto.MovimientoContableResponse;
import com.bcsystems.contabilidad.dto.MovimientoContableUpdateRequest;
import com.bcsystems.contabilidad.service.MovimientoContableService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/movimiento-contable")
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
    public ResponseEntity<?> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "idMovimientoContable") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<MovimientoContableResponse> result = movimientoContableService.findAllPageable(pageRequest);
        
        return ResponseEntity.status(HttpStatus.OK).body(result);
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
