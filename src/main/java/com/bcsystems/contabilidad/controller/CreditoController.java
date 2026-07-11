package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.service.CreditoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/creditos")
public class CreditoController {

    private final CreditoService creditoService;

    public CreditoController(CreditoService creditoService) {
        this.creditoService = creditoService;
    }

    @GetMapping("/clientes/{id}/creditos")
    public ResponseEntity<?> findByCliente(@PathVariable Integer id) {
        return ResponseEntity.ok(creditoService.findByCliente(id));
    }

    @GetMapping("/clientes/{id}/movimientos")
    public ResponseEntity<?> movimientosCliente(@PathVariable Integer id) {
        return ResponseEntity.ok(creditoService.getMovimientosCliente(id));
    }

    @PostMapping("/abonos")
    public ResponseEntity<?> registrarAbono(@RequestBody Map<String, Object> body) {
        Integer idCredito = Integer.valueOf(body.get("idCredito").toString());
        Double monto = Double.valueOf(body.get("monto").toString());
        String tipo = body.get("tipo") != null ? body.get("tipo").toString() : "PARCIAL";
        Integer idUsuario = body.get("idUsuario") != null ? Integer.valueOf(body.get("idUsuario").toString()) : 1;
        return ResponseEntity.status(HttpStatus.CREATED).body(
                creditoService.registrarAbono(idCredito, monto, tipo, idUsuario)
        );
    }

    @PostMapping("/abonos/general")
    public ResponseEntity<?> registrarAbonoGeneral(@RequestBody Map<String, Object> body) {
        Integer idCliente = Integer.valueOf(body.get("idCliente").toString());
        Double monto = Double.valueOf(body.get("monto").toString());
        Integer idUsuario = body.get("idUsuario") != null ? Integer.valueOf(body.get("idUsuario").toString()) : 1;
        return ResponseEntity.status(HttpStatus.CREATED).body(
                creditoService.registrarAbonoGeneral(idCliente, monto, idUsuario)
        );
    }
}
