package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.domain.Pedido;
import com.bcsystems.contabilidad.service.PedidoService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public ResponseEntity<?> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long idProveedor) {
        Sort sort = Sort.by("fechaCreacion").descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(pedidoService.buscarConFiltros(search, estado, idProveedor, pageRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(pedidoService.findById(id));
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody Pedido pedido) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.save(pedido));
    }

    @PostMapping("/{id}/recibir")
    public ResponseEntity<?> recibir(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> detalles = (List<Map<String, Object>>) body.get("detalles");
        return ResponseEntity.ok(pedidoService.recibir(id, detalles));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelar(@PathVariable Integer id) {
        return ResponseEntity.ok(pedidoService.cancelar(id));
    }

    @PostMapping("/{id}/completar")
    public ResponseEntity<?> completar(@PathVariable Integer id) {
        return ResponseEntity.ok(pedidoService.completar(id));
    }
}
