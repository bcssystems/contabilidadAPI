package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.dto.AsientoContableRegisterRequest;
import com.bcsystems.contabilidad.dto.AsientoContableResponse;
import com.bcsystems.contabilidad.dto.AsientoContableUpdateRequest;
import com.bcsystems.contabilidad.service.AsientoContableService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/asiento-contable")
public class AsientoContableController {

    private final AsientoContableService asientoContableService;

    public AsientoContableController(AsientoContableService asientoContableService) {
        this.asientoContableService = asientoContableService;
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody AsientoContableRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(asientoContableService.save(request));
    }

    @GetMapping
    public ResponseEntity<?> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "idAsientoContable") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<AsientoContableResponse> result = asientoContableService.findAllPageable(pageRequest);
        
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(asientoContableService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody AsientoContableUpdateRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(asientoContableService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        asientoContableService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
