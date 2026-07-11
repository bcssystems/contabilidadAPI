package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.domain.MovimientoStock;
import com.bcsystems.contabilidad.domain.en.TipoMovimiento;
import com.bcsystems.contabilidad.repository.MovimientoStockRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/kardex")
public class KardexController {

    private final MovimientoStockRepository movimientoStockRepository;

    public KardexController(MovimientoStockRepository movimientoStockRepository) {
        this.movimientoStockRepository = movimientoStockRepository;
    }

    @GetMapping
    public ResponseEntity<?> findByProducto(
            @RequestParam Integer idProducto,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Sort sort = Sort.by("fechaMovimiento").descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(
                movimientoStockRepository.findByProductoIdProductoOrderByFechaMovimientoDesc(idProducto, pageRequest)
        );
    }

    @GetMapping("/todo")
    public ResponseEntity<?> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(required = false) TipoMovimiento tipo) {
        Sort sort = Sort.by("fechaMovimiento").descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(
                movimientoStockRepository.findAllFiltered(fechaInicio, fechaFin, tipo, pageRequest)
        );
    }
}
