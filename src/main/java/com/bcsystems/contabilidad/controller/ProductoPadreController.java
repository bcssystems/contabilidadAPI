package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.dto.ProductoPadreRegisterRequest;
import com.bcsystems.contabilidad.dto.ProductoPadreResponse;
import com.bcsystems.contabilidad.dto.ProductoPadreUpdateRequest;
import com.bcsystems.contabilidad.service.ProductoPadreService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/productos-padre")
public class ProductoPadreController {

    private final ProductoPadreService productoPadreService;

    public ProductoPadreController(ProductoPadreService productoPadreService) {
        this.productoPadreService = productoPadreService;
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody ProductoPadreRegisterRequest request) {
        try {
            ProductoPadreResponse response = productoPadreService.save(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear producto padre: " + e.getMessage());
        }
    }

    @PostMapping("/con-variantes")
    public ResponseEntity<?> saveWithVariantes(@RequestBody ProductoPadreRegisterRequest request) {
        try {
            ProductoPadreResponse response = productoPadreService.saveWithVariantes(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear producto con variantes: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<ProductoPadreResponse> result = productoPadreService.findAllPageable(pageRequest);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return ResponseEntity.ok(productoPadreService.findResponseById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ProductoPadreUpdateRequest request) {
        try {
            ProductoPadreResponse response = productoPadreService.update(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar producto padre: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        productoPadreService.delete(id);
        return ResponseEntity.ok().build();
    }
}
