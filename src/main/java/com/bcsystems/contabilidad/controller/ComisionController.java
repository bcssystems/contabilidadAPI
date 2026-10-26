package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.dto.ComisionRegisterRequest;
import com.bcsystems.contabilidad.dto.ComisionResponse;
import com.bcsystems.contabilidad.dto.ComisionUpdateRequest;
import com.bcsystems.contabilidad.dto.ComisionBatchRequest;
import com.bcsystems.contabilidad.service.ComisionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import com.bcsystems.contabilidad.domain.en.EstadoComision;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/comision")
public class ComisionController {

    private final ComisionService comisionService;

    public ComisionController(ComisionService comisionService) {
        this.comisionService = comisionService;
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody ComisionRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(comisionService.save(request));
    }

    @PostMapping("/lote")
    public ResponseEntity<?> saveBatch(@RequestBody ComisionBatchRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(comisionService.saveBatch(request));
    }

    @PostMapping("/generar-por-venta")
    public ResponseEntity<?> generarComisionesVenta(
            @RequestParam Long ventaId,
            @RequestParam Long empleadoVendedorId,
            @RequestParam Long empleadoCobradorId,
            @RequestParam BigDecimal porcentajeVenta,
            @RequestParam BigDecimal porcentajeCobranza) {
        return ResponseEntity.status(HttpStatus.OK).body(
                comisionService.generateComisionesVenta(ventaId, empleadoVendedorId, 
                        empleadoCobradorId, porcentajeVenta, porcentajeCobranza));
    }

    @PostMapping("/pagar")
    public ResponseEntity<?> pagarComisiones(@RequestBody List<Long> comisionIds) {
        return ResponseEntity.status(HttpStatus.OK).body(comisionService.pagarComisiones(comisionIds));
    }

    @GetMapping
    public ResponseEntity<?> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "idComision") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) EstadoComision estadoComision,
            @RequestParam(required = false) Integer usuarioId,
            @RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<ComisionResponse> result = comisionService.findFiltered(pageRequest, search, estadoComision, usuarioId, desde, hasta);
        
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(comisionService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ComisionUpdateRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(comisionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        comisionService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/venta/{ventaId}")
    public ResponseEntity<?> findByVenta(@PathVariable Long ventaId) {
        return ResponseEntity.status(HttpStatus.OK).body(comisionService.findByVenta(ventaId));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> findByUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.status(HttpStatus.OK).body(comisionService.findByUsuario(usuarioId));
    }

    @GetMapping("/pendientes")
    public ResponseEntity<?> findPendientes() {
        return ResponseEntity.status(HttpStatus.OK).body(comisionService.findPendientes());
    }

    @GetMapping("/pendientes/usuario/{usuarioId}")
    public ResponseEntity<?> findPendientesByUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.status(HttpStatus.OK).body(comisionService.findPendientesByUsuario(usuarioId));
    }
}
