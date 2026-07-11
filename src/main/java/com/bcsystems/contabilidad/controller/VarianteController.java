package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.domain.Variante;
import com.bcsystems.contabilidad.dto.*;
import com.bcsystems.contabilidad.service.VarianteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/variantes")
public class VarianteController {

    private final VarianteService varianteService;
    private final ObjectMapper objectMapper;

    public VarianteController(VarianteService varianteService, ObjectMapper objectMapper) {
        this.varianteService = varianteService;
        this.objectMapper = objectMapper;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> save(
            @RequestPart("data") String dataJson,
            @RequestPart(value = "imagenes", required = false) List<MultipartFile> imagenes) {
        try {
            VarianteRegisterRequest request = objectMapper.readValue(dataJson, VarianteRegisterRequest.class);
            Variante variante = varianteService.save(request, imagenes);
            return ResponseEntity.status(HttpStatus.CREATED).body(varianteService.findResponseById(variante.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear variante: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> findAllByPadre(
            @RequestParam Long productoPadreId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<VarianteResponse> result = varianteService.findByProductoPadreId(productoPadreId, pageRequest);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/disponibles")
    public ResponseEntity<?> findDisponibles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("fechaEntrada").ascending());
        Page<VarianteResponse> result = varianteService.findDisponibles(pageRequest);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return ResponseEntity.ok(varianteService.findResponseById(id));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestPart("data") String dataJson,
            @RequestPart(value = "imagenes", required = false) List<MultipartFile> imagenes) {
        try {
            VarianteUpdateRequest request = objectMapper.readValue(dataJson, VarianteUpdateRequest.class);
            varianteService.update(id, request, imagenes);
            return ResponseEntity.ok(varianteService.findResponseById(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar variante: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        varianteService.delete(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/inventario-sucursal/{idSucursal}")
    public ResponseEntity<?> actualizarStockSucursal(
            @PathVariable Long id, @PathVariable Integer idSucursal,
            @RequestParam Integer stock) {
        try {
            return ResponseEntity.ok(varianteService.actualizarStockSucursal(id, idSucursal, stock));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar stock: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/movimiento-stock")
    public ResponseEntity<?> registrarMovimiento(
            @PathVariable Long id, @RequestBody MovimientoStockRequest request) {
        try {
            return ResponseEntity.ok(varianteService.registrarMovimientoStock(id, request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al registrar movimiento: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/transferir")
    public ResponseEntity<?> transferir(
            @PathVariable Long id, @RequestBody TransferenciaRequest request) {
        try {
            return ResponseEntity.ok(varianteService.transferirStock(id, request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al transferir stock: " + e.getMessage());
        }
    }
}
