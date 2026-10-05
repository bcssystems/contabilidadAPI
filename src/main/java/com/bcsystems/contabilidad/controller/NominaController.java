package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.dto.NominaRegisterRequest;
import com.bcsystems.contabilidad.dto.NominaResponse;
import com.bcsystems.contabilidad.dto.NominaUpdateRequest;
import com.bcsystems.contabilidad.service.NominaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/nomina")
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
    public ResponseEntity<?> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "idNomina") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<NominaResponse> result = nominaService.findAllPageable(pageRequest);
        
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/desglose/{usuarioId}")
    public ResponseEntity<?> desglose(@PathVariable Integer usuarioId) {
        return ResponseEntity.status(HttpStatus.OK).body(nominaService.desglose(usuarioId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(nominaService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody NominaUpdateRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(nominaService.update(id, request));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelar(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(nominaService.cancelar(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        nominaService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
