package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.service.GastoService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/gasto")
public class GastoController {

    private final GastoService gastoService;

    public GastoController(GastoService gastoService) {
        this.gastoService = gastoService;
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody Map<String, Object> body) {
        Integer idCaja = Integer.valueOf(body.get("idCaja").toString());
        String descripcion = body.get("descripcion").toString();
        Double monto = Double.valueOf(body.get("monto").toString());
        Integer idUsuario = body.get("idUsuario") != null ? Integer.valueOf(body.get("idUsuario").toString()) : 1;
        return ResponseEntity.status(HttpStatus.CREATED).body(gastoService.save(idCaja, descripcion, monto, idUsuario));
    }

    @GetMapping
    public ResponseEntity<?> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Sort sort = Sort.by("fechaCreacion").descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(gastoService.findAllPageable(pageRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(gastoService.findById(id));
    }

    @GetMapping("/pendientes")
    public ResponseEntity<?> findPendientes() {
        return ResponseEntity.ok(gastoService.findPendientes());
    }

    @GetMapping("/caja/{idCaja}")
    public ResponseEntity<?> findByCaja(@PathVariable Integer idCaja) {
        return ResponseEntity.ok(gastoService.findByCaja(idCaja));
    }

    @PostMapping("/{id}/{accion}")
    public ResponseEntity<?> autorizarRechazar(@PathVariable Integer id, @PathVariable String accion) {
        Integer idAutorizador = 1;
        if ("autorizar".equalsIgnoreCase(accion)) {
            return ResponseEntity.ok(gastoService.autorizar(id, idAutorizador));
        } else if ("rechazar".equalsIgnoreCase(accion)) {
            return ResponseEntity.ok(gastoService.rechazar(id, idAutorizador));
        }
        return ResponseEntity.badRequest().body("Accion no valida: " + accion);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        gastoService.delete(id);
        return ResponseEntity.ok().build();
    }
}
