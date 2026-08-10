package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.domain.Persona;
import com.bcsystems.contabilidad.domain.Usuario;
import com.bcsystems.contabilidad.domain.Venta;
import com.bcsystems.contabilidad.domain.VentaDetalle;
import com.bcsystems.contabilidad.domain.VentaPago;
import com.bcsystems.contabilidad.domain.en.EstadoVenta;
import com.bcsystems.contabilidad.service.VentaService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/venta")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody Map<String, Object> body, Authentication authentication) {
        Venta venta = new Venta();
        if (body.get("idCaja") != null) {
            com.bcsystems.contabilidad.domain.Caja caja = new com.bcsystems.contabilidad.domain.Caja();
            caja.setIdCaja(Integer.valueOf(body.get("idCaja").toString()));
            venta.setCaja(caja);
        }
        if (body.get("idCliente") != null) {
            com.bcsystems.contabilidad.domain.Cliente cliente = new com.bcsystems.contabilidad.domain.Cliente();
            cliente.setIdCliente(Integer.valueOf(body.get("idCliente").toString()));
            venta.setCliente(cliente);
        }
        Persona vendedor = null;
        if (authentication != null && authentication.getPrincipal() instanceof Usuario) {
            vendedor = ((Usuario) authentication.getPrincipal()).getPersona();
        }
        if (vendedor == null || vendedor.getIdPerson() == null) {
            vendedor = null;
        }
        if (body.get("idUsuario") != null && vendedor == null) {
            vendedor = new Persona();
            vendedor.setIdPerson(Integer.valueOf(body.get("idUsuario").toString()));
        }
        venta.setTipoVenta(com.bcsystems.contabilidad.domain.en.TipoVenta.valueOf(body.get("tipoVenta").toString()));
        venta.setPrecioSeleccionado(body.get("precioSeleccionado") != null ? Integer.valueOf(body.get("precioSeleccionado").toString()) : 1);
        venta.setSubtotal(Double.valueOf(body.get("subtotal").toString()));
        venta.setDescuento(body.get("descuento") != null ? Double.valueOf(body.get("descuento").toString()) : 0.0);
        venta.setTotal(Double.valueOf(body.get("total").toString()));
        venta.setNota(body.get("nota") != null ? body.get("nota").toString() : null);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> detallesMap = (List<Map<String, Object>>) body.get("detalles");
        List<VentaDetalle> detalles = new java.util.ArrayList<>();
        if (detallesMap != null) {
            for (Map<String, Object> d : detallesMap) {
                VentaDetalle detalle = new VentaDetalle();
                if (d.get("idProducto") != null) {
                    com.bcsystems.contabilidad.domain.Producto producto = new com.bcsystems.contabilidad.domain.Producto();
                    producto.setIdProducto(Integer.valueOf(d.get("idProducto").toString()));
                    detalle.setProducto(producto);
                }
                detalle.setDescripcion(d.get("descripcion") != null ? d.get("descripcion").toString() : null);
                detalle.setCantidad(Integer.valueOf(d.get("cantidad").toString()));
                detalle.setPrecioUnitario(Double.valueOf(d.get("precioUnitario").toString()));
                detalle.setSubtotal(Double.valueOf(d.get("subtotal").toString()));
                detalles.add(detalle);
            }
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> pagosMap = (List<Map<String, Object>>) body.get("pagos");
        List<VentaPago> pagos = new java.util.ArrayList<>();
        if (pagosMap != null) {
            for (Map<String, Object> p : pagosMap) {
                VentaPago pago = new VentaPago();
                if (p.get("idTipoPago") != null) {
                    com.bcsystems.contabilidad.domain.TipoPago tipoPago = new com.bcsystems.contabilidad.domain.TipoPago();
                    tipoPago.setIdTipoPago(Integer.valueOf(p.get("idTipoPago").toString()));
                    pago.setTipoPago(tipoPago);
                }
                pago.setMonto(Double.valueOf(p.get("monto").toString()));
                pago.setReferencia(p.get("referencia") != null ? p.get("referencia").toString() : null);
                pagos.add(pago);
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(ventaService.save(venta, detalles, pagos, vendedor));
    }

    @GetMapping
    public ResponseEntity<?> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer idSucursal,
            @RequestParam(required = false) Integer idCaja,
            @RequestParam(required = false) EstadoVenta estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        Sort sort = Sort.by("fecha").descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(ventaService.buscarConFiltros(idSucursal, idCaja, estado, fechaInicio, fechaFin, pageRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(ventaService.findById(id));
    }

    @GetMapping("/caja/{idCaja}")
    public ResponseEntity<?> findByCaja(@PathVariable Integer idCaja) {
        return ResponseEntity.ok(ventaService.findByCaja(idCaja));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelar(@PathVariable Integer id) {
        return ResponseEntity.ok(ventaService.cancelar(id));
    }
}
