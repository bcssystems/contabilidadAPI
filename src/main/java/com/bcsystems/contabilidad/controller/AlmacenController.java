package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.dto.MovimientoAlmacenRegisterRequest;
import com.bcsystems.contabilidad.dto.MovimientoAlmacenResponse;
import com.bcsystems.contabilidad.service.AlmacenService;
import com.bcsystems.contabilidad.service.CostoPromedioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.bcsystems.contabilidad.domain.Usuario;
import com.bcsystems.contabilidad.repository.UsuarioRepository;

@RestController
@RequestMapping("/api/v1/almacen")
public class AlmacenController {

    private final AlmacenService almacenService;
    private final CostoPromedioService costoPromedioService;
    private final UsuarioRepository usuarioRepository;

    public AlmacenController(AlmacenService almacenService,
                             CostoPromedioService costoPromedioService,
                             UsuarioRepository usuarioRepository) {
        this.almacenService = almacenService;
        this.costoPromedioService = costoPromedioService;
        this.usuarioRepository = usuarioRepository;
    }

    private Integer getUsuarioId(Authentication auth) {
        Usuario usuario = (Usuario) auth.getPrincipal();
        return usuario.getIdUser();
    }

    @PostMapping("/entrada")
    public ResponseEntity<?> registrarEntrada(
            @RequestBody MovimientoAlmacenRegisterRequest request,
            Authentication authentication) {
        try {
            MovimientoAlmacenResponse response = almacenService.registrarEntrada(request, getUsuarioId(authentication));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al registrar entrada: " + e.getMessage());
        }
    }

    @PostMapping("/salida-solicitar")
    public ResponseEntity<?> solicitarSalida(
            @RequestBody MovimientoAlmacenRegisterRequest request,
            Authentication authentication) {
        try {
            MovimientoAlmacenResponse response = almacenService.solicitarSalida(request, getUsuarioId(authentication));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al solicitar salida: " + e.getMessage());
        }
    }

    @PostMapping("/aprobar/{id}")
    public ResponseEntity<?> aprobarSalida(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            MovimientoAlmacenResponse response = almacenService.aprobarSalida(id, getUsuarioId(authentication));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al aprobar salida: " + e.getMessage());
        }
    }

    @PostMapping("/rechazar/{id}")
    public ResponseEntity<?> rechazarSalida(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            MovimientoAlmacenResponse response = almacenService.rechazarSalida(id, getUsuarioId(authentication));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al rechazar salida: " + e.getMessage());
        }
    }

    @GetMapping("/pendientes")
    public ResponseEntity<?> findPendientes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("fechaSolicitud").ascending());
        Page<MovimientoAlmacenResponse> result = almacenService.findPendientes(pageRequest);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/movimientos")
    public ResponseEntity<?> findAllMovimientos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("fechaSolicitud").descending());
        Page<MovimientoAlmacenResponse> result = almacenService.findAllMovimientos(pageRequest);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/movimientos/{id}")
    public ResponseEntity<?> findMovimientoById(@PathVariable Long id) {
        return ResponseEntity.ok(almacenService.findById(id));
    }

    @GetMapping("/salida-pdf/{id}")
    public ResponseEntity<?> generarPdf(@PathVariable Long id) {
        try {
            byte[] pdf = almacenService.generarPdfSalida(id);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("filename", "salida-almacen-" + id + ".pdf");
            return ResponseEntity.ok().headers(headers).body(pdf);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al generar PDF: " + e.getMessage());
        }
    }

    @GetMapping("/costo-promedio/{productoPadreId}")
    public ResponseEntity<?> getCostoPromedio(@PathVariable Long productoPadreId) {
        try {
            return ResponseEntity.ok(costoPromedioService.getCostoPromedioByProductoPadreId(productoPadreId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al obtener costo promedio: " + e.getMessage());
        }
    }

    @PostMapping("/costo-promedio/recalcular/{productoPadreId}")
    public ResponseEntity<?> recalcularCostoPromedio(@PathVariable Long productoPadreId) {
        try {
            return ResponseEntity.ok(costoPromedioService.recalcular(productoPadreId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al recalcular costo promedio: " + e.getMessage());
        }
    }

    @GetMapping("/usuarios-responsables")
    public ResponseEntity<?> getUsuariosResponsables() {
        return ResponseEntity.ok(usuarioRepository.findByActivoTrue());
    }
}
