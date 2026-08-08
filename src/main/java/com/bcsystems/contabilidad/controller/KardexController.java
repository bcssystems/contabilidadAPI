package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.domain.en.TipoMovimiento;
import com.bcsystems.contabilidad.dto.KardexUnificadoResponse;
import com.bcsystems.contabilidad.dto.MovimientoStockResponse;
import com.bcsystems.contabilidad.service.KardexService;
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

    private final KardexService kardexService;

    public KardexController(KardexService kardexService) {
        this.kardexService = kardexService;
    }

    @GetMapping
    public ResponseEntity<Page<MovimientoStockResponse>> findByProducto(
            @RequestParam Integer idProducto,
            @RequestParam(required = false) Integer idSucursal,
            @RequestParam(required = false) TipoMovimiento tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Sort sort = Sort.by("fechaMovimiento").descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(
                kardexService.listarMovimientos(idProducto, idSucursal, tipo, fechaInicio, fechaFin, pageRequest)
        );
    }

    @GetMapping("/todo")
    public ResponseEntity<Page<KardexUnificadoResponse>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin,
            @RequestParam(required = false) TipoMovimiento tipo) {
        Sort sort = Sort.by("fechaMovimiento").descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(
                kardexService.listarTodo(fechaInicio, fechaFin, tipo, pageRequest)
        );
    }
}
