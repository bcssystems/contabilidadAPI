package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.dto.*;
import com.bcsystems.contabilidad.service.PrestamoEmpleadoService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.bcsystems.contabilidad.domain.en.EstadoPrestamo;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/prestamos")
public class PrestamoEmpleadoController {
    private final PrestamoEmpleadoService service;
    public PrestamoEmpleadoController(PrestamoEmpleadoService service) { this.service = service; }

    @PostMapping public ResponseEntity<?> crear(@RequestBody PrestamoEmpleadoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }
    @GetMapping public ResponseEntity<?> listar(@RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size,
                                                @RequestParam(required = false) String search,
                                                @RequestParam(required = false) EstadoPrestamo estado) {
        return ResponseEntity.ok(service.listarFiltrado(PageRequest.of(page, size, Sort.by("idPrestamo").descending()), search, estado));
    }
    @GetMapping("/{id}") public ResponseEntity<?> detalle(@PathVariable Long id) { return ResponseEntity.ok(service.detalle(id)); }
    @GetMapping("/{id}/parcialidades") public ResponseEntity<?> parcialidades(@PathVariable Long id) { return ResponseEntity.ok(service.parcialidades(id)); }
    @PostMapping("/deducciones/generar") public ResponseEntity<?> generarDeducciones(@RequestBody(required = false) java.util.Map<String, String> body) {
        LocalDate fecha = body != null && body.get("fecha") != null ? LocalDate.parse(body.get("fecha")) : LocalDate.now();
        service.generarDeduccionesParaTodos(fecha);
        return ResponseEntity.ok().build();
    }
    @PutMapping("/{id}") public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody PrestamoEmpleadoUpdateRequest request) { return ResponseEntity.ok(service.actualizar(id, request)); }
    @PostMapping("/{id}/cancelar") public ResponseEntity<?> cancelar(@PathVariable Long id) { return ResponseEntity.ok(service.cancelar(id)); }
}
