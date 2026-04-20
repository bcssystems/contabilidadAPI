package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.dto.CuentaContableRegisterRequest;
import com.bcsystems.contabilidad.dto.CuentaContableResponse;
import com.bcsystems.contabilidad.dto.CuentaContableUpdateRequest;
import com.bcsystems.contabilidad.service.CuentaContableService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cuenta-contable")
public class CuentaContableController {

    private final CuentaContableService cuentaContableService;

    public CuentaContableController(CuentaContableService cuentaContableService) {
        this.cuentaContableService = cuentaContableService;
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody CuentaContableRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(cuentaContableService.save(request));
    }

    @GetMapping
    public ResponseEntity<?> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "300") int size,
            @RequestParam(defaultValue = "idCuentaContable") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<CuentaContableResponse> result = cuentaContableService.findAllPageable(pageRequest);
        
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(cuentaContableService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody CuentaContableUpdateRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(cuentaContableService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        cuentaContableService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
