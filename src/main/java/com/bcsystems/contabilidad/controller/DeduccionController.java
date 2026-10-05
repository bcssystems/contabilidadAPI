package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.dto.DeduccionRegisterRequest;
import com.bcsystems.contabilidad.dto.DeduccionResponse;
import com.bcsystems.contabilidad.dto.DeduccionUpdateRequest;
import com.bcsystems.contabilidad.service.DeduccionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/deduccion")
public class DeduccionController {

    private final DeduccionService deduccionService;

    public DeduccionController(DeduccionService deduccionService) {
        this.deduccionService = deduccionService;
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody DeduccionRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(deduccionService.save(request));
    }

    @GetMapping
    public ResponseEntity<?> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "idDeduccion") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<DeduccionResponse> result = deduccionService.findAllPageable(pageRequest);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> findByUsuario(@PathVariable Integer usuarioId) {
        return ResponseEntity.status(HttpStatus.OK).body(deduccionService.findByUsuario(usuarioId));
    }

    @GetMapping("/pendientes/usuario/{usuarioId}")
    public ResponseEntity<?> findPendientesByUsuario(@PathVariable Integer usuarioId) {
        return ResponseEntity.status(HttpStatus.OK).body(deduccionService.findPendientesByUsuarioResponse(usuarioId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(deduccionService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody DeduccionUpdateRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(deduccionService.update(id, request));
    }

    @PostMapping("/{id}/aprobar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approve(@PathVariable Long id) {
        return ResponseEntity.ok(deduccionService.reviewAttendance(id, true));
    }

    @PostMapping("/{id}/rechazar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> reject(@PathVariable Long id) {
        return ResponseEntity.ok(deduccionService.reviewAttendance(id, false));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        deduccionService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
