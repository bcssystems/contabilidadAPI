package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.dto.NominaRegisterRequest;
import com.bcsystems.contabilidad.dto.NominaUpdateRequest;
import com.bcsystems.contabilidad.service.NominaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/nomina")
public class NominaController {

    private final NominaService nominaService;

    public NominaController(NominaService nominaService) {
        this.nominaService = nominaService;
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody NominaRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(nominaService.save(request));
    }

    @GetMapping
    public ResponseEntity<?> findAll() {
        return ResponseEntity.status(HttpStatus.OK).body(nominaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(nominaService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody NominaUpdateRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(nominaService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        nominaService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
