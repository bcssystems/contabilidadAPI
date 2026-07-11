package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.domain.Promocion;
import com.bcsystems.contabilidad.domain.en.TipoPromocion;
import com.bcsystems.contabilidad.service.PromocionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/promociones")
public class PromocionController {

    private final PromocionService promocionService;

    public PromocionController(PromocionService promocionService) {
        this.promocionService = promocionService;
    }

    @GetMapping
    public ResponseEntity<?> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) TipoPromocion tipo,
            @RequestParam(required = false) Boolean activo) {
        Sort sort = Sort.by("fechaCreacion").descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(promocionService.buscarConFiltros(tipo, activo, pageRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(promocionService.findById(id));
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody Promocion promocion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(promocionService.save(promocion));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody Promocion promocion) {
        return ResponseEntity.ok(promocionService.update(id, promocion));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        promocionService.delete(id);
        return ResponseEntity.ok().build();
    }
}
