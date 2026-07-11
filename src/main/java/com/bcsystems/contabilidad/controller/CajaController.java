package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.domain.Caja;
import com.bcsystems.contabilidad.service.CajaService;
import com.bcsystems.contabilidad.service.CorteCajaService;
import com.bcsystems.contabilidad.service.MovimientoCajaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/cajas")
public class CajaController {

    private final CajaService cajaService;
    private final MovimientoCajaService movimientoCajaService;
    private final CorteCajaService corteCajaService;

    public CajaController(CajaService cajaService,
                          MovimientoCajaService movimientoCajaService,
                          CorteCajaService corteCajaService) {
        this.cajaService = cajaService;
        this.movimientoCajaService = movimientoCajaService;
        this.corteCajaService = corteCajaService;
    }

    @GetMapping
    public ResponseEntity<?> findAll(@RequestParam(required = false) Integer filtroSucursal) {
        if (filtroSucursal != null) {
            return ResponseEntity.ok(cajaService.findBySucursal(filtroSucursal));
        }
        return ResponseEntity.ok(cajaService.findAll());
    }

    @GetMapping("/sucursal/{idSucursal}")
    public ResponseEntity<?> findBySucursal(@PathVariable Integer idSucursal) {
        return ResponseEntity.ok(cajaService.findBySucursal(idSucursal));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(cajaService.findById(id));
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody Caja caja) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cajaService.save(caja));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody Caja caja) {
        return ResponseEntity.ok(cajaService.update(id, caja));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        cajaService.delete(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/apertura")
    public ResponseEntity<?> apertura(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        Double saldoInicial = body.get("saldoInicial") != null ? Double.valueOf(body.get("saldoInicial").toString()) : null;
        return ResponseEntity.ok(cajaService.apertura(id, saldoInicial));
    }

    @PostMapping("/{id}/cierre")
    public ResponseEntity<?> cierre(@PathVariable Integer id) {
        return ResponseEntity.ok(cajaService.cierre(id));
    }

    @PostMapping("/{id}/ingresos")
    public ResponseEntity<?> ingreso(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        Double monto = Double.valueOf(body.get("monto").toString());
        String motivo = body.get("motivo").toString();
        Integer idUsuario = body.get("idUsuario") != null ? Integer.valueOf(body.get("idUsuario").toString()) : 1;
        return ResponseEntity.status(HttpStatus.CREATED).body(
                movimientoCajaService.registrarIngreso(id, monto, motivo, idUsuario)
        );
    }

    @PostMapping("/{id}/egresos")
    public ResponseEntity<?> egreso(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        Double monto = Double.valueOf(body.get("monto").toString());
        String motivo = body.get("motivo").toString();
        Integer idUsuario = body.get("idUsuario") != null ? Integer.valueOf(body.get("idUsuario").toString()) : 1;
        return ResponseEntity.status(HttpStatus.CREATED).body(
                movimientoCajaService.registrarEgreso(id, monto, motivo, idUsuario)
        );
    }

    @GetMapping("/{id}/corte-preview")
    public ResponseEntity<?> cortePreview(@PathVariable Integer id) {
        return ResponseEntity.ok(corteCajaService.previewCorte(id));
    }

    @PostMapping("/{id}/corte")
    public ResponseEntity<?> corte(@PathVariable Integer id, @RequestBody(required = false) Map<String, Object> body) {
        Integer idUsuario = body != null && body.get("idUsuario") != null
                ? Integer.valueOf(body.get("idUsuario").toString()) : 1;
        return ResponseEntity.ok(corteCajaService.realizarCorte(id, idUsuario));
    }

    @GetMapping("/{id}/movimientos")
    public ResponseEntity<?> movimientos(@PathVariable Integer id) {
        return ResponseEntity.ok(movimientoCajaService.findByCaja(id));
    }
}
