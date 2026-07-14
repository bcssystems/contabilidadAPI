package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.domain.Producto;
import com.bcsystems.contabilidad.domain.ProductoMultimedia;
import com.bcsystems.contabilidad.domain.en.TipoMultimedia;
import com.bcsystems.contabilidad.repository.ProductoMultimediaRepository;
import com.bcsystems.contabilidad.repository.ProductoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/productos/{idProducto}/multimedia")
public class ProductoMultimediaController {

    private final ProductoMultimediaRepository multimediaRepository;
    private final ProductoRepository productoRepository;

    public ProductoMultimediaController(ProductoMultimediaRepository multimediaRepository,
                                        ProductoRepository productoRepository) {
        this.multimediaRepository = multimediaRepository;
        this.productoRepository = productoRepository;
    }

    @PostMapping
    public ResponseEntity<?> upload(
            @PathVariable Integer idProducto,
            @RequestParam("archivo") org.springframework.web.multipart.MultipartFile archivo,
            @RequestParam(value = "esPrincipal", defaultValue = "false") String esPrincipal) {
        try {
            Producto producto = productoRepository.findById(idProducto)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            String fileName = System.currentTimeMillis() + "_" + archivo.getOriginalFilename();
            String uploadDir = "uploads/productos/";
            java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);
            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
            }
            java.nio.file.Path filePath = uploadPath.resolve(fileName);
            java.nio.file.Files.copy(archivo.getInputStream(), filePath,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            ProductoMultimedia multimedia = ProductoMultimedia.builder()
                    .producto(producto)
                    .tipo(TipoMultimedia.IMAGEN)
                    .url("/api/images/" + fileName)
                    .nombreArchivo(archivo.getOriginalFilename())
                    .esPrincipal("true".equalsIgnoreCase(esPrincipal))
                    .build();
            multimedia = multimediaRepository.save(multimedia);

            return ResponseEntity.status(HttpStatus.CREATED).body(multimedia);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al subir archivo: " + e.getMessage());
        }
    }

    @DeleteMapping("/{idMultimedia}")
    public ResponseEntity<?> delete(@PathVariable Integer idProducto, @PathVariable Integer idMultimedia) {
        multimediaRepository.deleteById(idMultimedia);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<?> findByProducto(@PathVariable Integer idProducto) {
        return ResponseEntity.ok(multimediaRepository.findByProductoIdProductoOrderByEsPrincipalDesc(idProducto));
    }
}
