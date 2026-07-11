package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.domain.Producto;
import com.bcsystems.contabilidad.repository.*;
import com.bcsystems.contabilidad.service.ProductoService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/productos")
public class ProductoController {

    private final ProductoService productoService;
    private final ProductoRepository productoRepository;
    private final ProductoMultimediaRepository productoMultimediaRepository;

    public ProductoController(ProductoService productoService,
                              ProductoRepository productoRepository,
                              ProductoMultimediaRepository productoMultimediaRepository) {
        this.productoService = productoService;
        this.productoRepository = productoRepository;
        this.productoMultimediaRepository = productoMultimediaRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<?> stats() {
        return ResponseEntity.ok(productoService.getStats());
    }

    @GetMapping
    public ResponseEntity<?> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "nombre") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer idSucursal,
            @RequestParam(required = false) Boolean activo) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        if (idSucursal != null) {
            return ResponseEntity.ok(productoService.findBySucursal(idSucursal, pageRequest));
        }
        return ResponseEntity.ok(productoService.buscarConFiltros(search, activo, null, pageRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(productoService.findById(id));
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody Producto producto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.save(producto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody Producto producto) {
        return ResponseEntity.ok(productoService.update(id, producto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        productoService.delete(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/multimedia/{id}")
    public ResponseEntity<?> deleteMultimedia(@PathVariable Integer id) {
        productoMultimediaRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/transferir")
    public ResponseEntity<?> transferir(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        Integer idSucursalOrigen = Integer.valueOf(body.get("idSucursalOrigen").toString());
        Integer idSucursalDestino = Integer.valueOf(body.get("idSucursalDestino").toString());
        Integer cantidad = Integer.valueOf(body.get("cantidad").toString());
        String referencia = body.get("referencia") != null ? body.get("referencia").toString() : null;
        String observacion = body.get("observacion") != null ? body.get("observacion").toString() : null;
        return ResponseEntity.ok(productoService.transferirStock(id, idSucursalOrigen, idSucursalDestino, cantidad, referencia, observacion));
    }

    @PostMapping("/{id}/movimiento-stock")
    public ResponseEntity<?> movimientoStock(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        String tipoMovimiento = body.get("tipoMovimiento").toString();
        Integer cantidad = Integer.valueOf(body.get("cantidad").toString());
        Integer idSucursal = body.get("idSucursal") != null ? Integer.valueOf(body.get("idSucursal").toString()) : null;
        String referencia = body.get("referencia") != null ? body.get("referencia").toString() : null;
        String observacion = body.get("observacion") != null ? body.get("observacion").toString() : null;
        return ResponseEntity.ok(productoService.registrarMovimientoStock(id, tipoMovimiento, cantidad, idSucursal, referencia, observacion));
    }
}
