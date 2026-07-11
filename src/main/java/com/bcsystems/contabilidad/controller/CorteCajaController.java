package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.service.CorteCajaService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/cortes")
public class CorteCajaController {

    private final CorteCajaService corteCajaService;

    public CorteCajaController(CorteCajaService corteCajaService) {
        this.corteCajaService = corteCajaService;
    }

    @GetMapping
    public ResponseEntity<?> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer idSucursal,
            @RequestParam(required = false) Integer idCaja,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        Sort sort = Sort.by("fechaCierre").descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(corteCajaService.buscarConFiltros(idSucursal, idCaja, fechaInicio, fechaFin, pageRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(corteCajaService.findById(id));
    }
}
