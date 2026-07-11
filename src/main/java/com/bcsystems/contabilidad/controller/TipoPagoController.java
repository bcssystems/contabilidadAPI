package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.domain.TipoPago;
import com.bcsystems.contabilidad.service.TipoPagoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tipos-pago")
public class TipoPagoController {

    private final TipoPagoService tipoPagoService;

    public TipoPagoController(TipoPagoService tipoPagoService) {
        this.tipoPagoService = tipoPagoService;
    }

    @GetMapping
    public ResponseEntity<?> findAll() {
        return ResponseEntity.ok(tipoPagoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(tipoPagoService.findById(id));
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody TipoPago tipoPago) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tipoPagoService.save(tipoPago));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody TipoPago tipoPago) {
        return ResponseEntity.ok(tipoPagoService.update(id, tipoPago));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        tipoPagoService.delete(id);
        return ResponseEntity.ok().build();
    }
}
