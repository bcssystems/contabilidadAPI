package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.domain.Producto;
import com.bcsystems.contabilidad.domain.en.CategoriaProducto;
import com.bcsystems.contabilidad.domain.en.EstadoProducto;
import com.bcsystems.contabilidad.dto.ProductoAdminResponse;
import com.bcsystems.contabilidad.dto.ProductoEcommerceResponse;
import com.bcsystems.contabilidad.dto.ProductoRegisterRequest;
import com.bcsystems.contabilidad.dto.ProductoUpdateRequest;
import com.bcsystems.contabilidad.service.ProductoService;
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
@RequestMapping("/api/v1/productos")
public class ProductoController {

    private final ProductoService productoService;
    private final ObjectMapper objectMapper;

    public ProductoController(ProductoService productoService, ObjectMapper objectMapper) {
        this.productoService = productoService;
        this.objectMapper = objectMapper;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> save(
            @RequestPart("data") String dataJson,
            @RequestPart(value = "imagenes", required = false) List<MultipartFile> imagenes) {
        try {
            ProductoRegisterRequest request = objectMapper.readValue(dataJson, ProductoRegisterRequest.class);
            Producto producto = productoService.save(request, imagenes);
            return ResponseEntity.status(HttpStatus.CREATED).body(productoService.findAdminById(producto.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear producto: " + e.getMessage());
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
        Page<ProductoAdminResponse> result = productoService.findAllPageable(pageRequest);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(productoService.findAdminById(id));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestPart("data") String dataJson,
            @RequestPart(value = "imagenes", required = false) List<MultipartFile> imagenes) {
        try {
            ProductoUpdateRequest request = objectMapper.readValue(dataJson, ProductoUpdateRequest.class);
            productoService.update(id, request, imagenes);
            return ResponseEntity.status(HttpStatus.OK).body(productoService.findAdminById(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar producto: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        productoService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/ecommerce")
    public ResponseEntity<?> findAllEcommerce(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) CategoriaProducto categoria,
            @RequestParam(required = false) EstadoProducto estado) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<ProductoEcommerceResponse> result;

        if (categoria != null && estado != null) {
            result = productoService.findByCategoriaAndEstadoEcommerce(pageRequest, categoria, estado);
        } else if (categoria != null) {
            result = productoService.findByCategoriaEcommerce(pageRequest, categoria);
        } else if (estado != null) {
            result = productoService.findByEstadoEcommerce(pageRequest, estado);
        } else {
            result = productoService.findAllEcommercePageable(pageRequest);
        }

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/ecommerce/{id}")
    public ResponseEntity<?> findEcommerceById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(productoService.findEcommerceById(id));
    }
}
