package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Producto;
import com.bcsystems.contabilidad.domain.en.CategoriaProducto;
import com.bcsystems.contabilidad.domain.en.EstadoProducto;
import com.bcsystems.contabilidad.dto.ProductoAdminResponse;
import com.bcsystems.contabilidad.dto.ProductoEcommerceResponse;
import com.bcsystems.contabilidad.dto.ProductoRegisterRequest;
import com.bcsystems.contabilidad.dto.ProductoUpdateRequest;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.ProductoRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.upload.dir:uploads/productos}")
    private String uploadDir;

    public ProductoServiceImpl(ProductoRepository productoRepository, ObjectMapper objectMapper) {
        this.productoRepository = productoRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public Producto save(ProductoRegisterRequest request, List<MultipartFile> imagenesFiles) {
        List<String> rutasImagenes = saveImageFiles(imagenesFiles);

        Producto producto = Producto.builder()
                .nombre(request.nombre())
                .marca(request.marca())
                .modelo(request.modelo())
                .precioCompra(request.precioCompra())
                .precioVenta(request.precioVenta())
                .estado(request.estado())
                .categoria(request.categoria())
                .descripcion(request.descripcion())
                .especificaciones(toJson(request.especificaciones()))
                .mensajeWhatsapp(request.mensajeWhatsapp() != null ? request.mensajeWhatsapp() :
                        "Hola, me interesa este producto. Podrian darme mas informacion?")
                .pagadoCaja(request.pagadoCaja() != null && request.pagadoCaja())
                .imagenes(toJson(rutasImagenes))
                .build();

        return productoRepository.save(producto);
    }

    @Override
    public Page<ProductoAdminResponse> findAllPageable(PageRequest pageRequest) {
        Page<Producto> productos = productoRepository.findByActivoTrue(pageRequest);
        return productos.map(this::toAdminResponse);
    }

    @Override
    public Producto findById(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));
    }

    @Override
    public ProductoAdminResponse findAdminById(Long id) {
        Producto producto = findById(id);
        return toAdminResponse(producto);
    }

    @Override
    @Transactional
    public Producto update(Long id, ProductoUpdateRequest request, List<MultipartFile> imagenesFiles) {
        Producto producto = findById(id);

        if (request.nombre() != null) producto.setNombre(request.nombre());
        if (request.marca() != null) producto.setMarca(request.marca());
        if (request.modelo() != null) producto.setModelo(request.modelo());
        if (request.precioCompra() != null) producto.setPrecioCompra(request.precioCompra());
        if (request.precioVenta() != null) producto.setPrecioVenta(request.precioVenta());
        if (request.estado() != null) producto.setEstado(request.estado());
        if (request.categoria() != null) producto.setCategoria(request.categoria());
        if (request.descripcion() != null) producto.setDescripcion(request.descripcion());
        if (request.especificaciones() != null) producto.setEspecificaciones(toJson(request.especificaciones()));
        if (request.mensajeWhatsapp() != null) producto.setMensajeWhatsapp(request.mensajeWhatsapp());
        if (request.pagadoCaja() != null) producto.setPagadoCaja(request.pagadoCaja());
        if (request.activo() != null) producto.setActivo(request.activo());

        if (request.imagenesEliminar() != null && !request.imagenesEliminar().isEmpty()) {
            List<String> imagenesExistentes = fromJson(producto.getImagenes());
            List<String> imagenesActualizadas = new ArrayList<>(imagenesExistentes);
            for (String img : request.imagenesEliminar()) {
                imagenesActualizadas.remove(img);
                deleteImageFile(img);
            }
            producto.setImagenes(toJson(imagenesActualizadas));
        }

        if (imagenesFiles != null && !imagenesFiles.isEmpty()) {
            List<String> rutasImagenes = saveImageFiles(imagenesFiles);
            List<String> imagenesExistentes = fromJson(producto.getImagenes());
            imagenesExistentes.addAll(rutasImagenes);
            producto.setImagenes(toJson(imagenesExistentes));
        }

        return productoRepository.save(producto);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Producto producto = findById(id);
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    @Override
    public Page<ProductoEcommerceResponse> findAllEcommercePageable(PageRequest pageRequest) {
        Page<Producto> productos = productoRepository.findByActivoTrue(pageRequest);
        return productos.map(this::toEcommerceResponse);
    }

    @Override
    public ProductoEcommerceResponse findEcommerceById(Long id) {
        Producto producto = findById(id);
        return toEcommerceResponse(producto);
    }

    @Override
    public Page<ProductoEcommerceResponse> findByCategoriaEcommerce(PageRequest pageRequest, CategoriaProducto categoria) {
        Page<Producto> productos = productoRepository.findByActivoTrueAndCategoria(pageRequest, categoria);
        return productos.map(this::toEcommerceResponse);
    }

    @Override
    public Page<ProductoEcommerceResponse> findByEstadoEcommerce(PageRequest pageRequest, EstadoProducto estado) {
        Page<Producto> productos = productoRepository.findByActivoTrueAndEstado(pageRequest, estado);
        return productos.map(this::toEcommerceResponse);
    }

    @Override
    public Page<ProductoEcommerceResponse> findByCategoriaAndEstadoEcommerce(PageRequest pageRequest, CategoriaProducto categoria, EstadoProducto estado) {
        Page<Producto> productos = productoRepository.findByActivoTrueAndCategoriaAndEstado(pageRequest, categoria, estado);
        return productos.map(this::toEcommerceResponse);
    }

    private List<String> saveImageFiles(List<MultipartFile> files) {
        List<String> rutas = new ArrayList<>();
        if (files == null || files.isEmpty()) return rutas;

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;
                String originalFilename = file.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String uniqueFilename = UUID.randomUUID().toString() + extension;
                Path filePath = uploadPath.resolve(uniqueFilename);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                rutas.add(uniqueFilename);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar imágenes", e);
        }

        return rutas;
    }

    private ProductoAdminResponse toAdminResponse(Producto p) {
        return new ProductoAdminResponse(
                p.getId(),
                p.getNombre(),
                p.getMarca(),
                p.getModelo(),
                p.getPrecioCompra(),
                p.getPrecioVenta(),
                p.getEstado(),
                p.getCategoria(),
                fromJson(p.getImagenes()),
                p.getDescripcion(),
                fromJson(p.getEspecificaciones()),
                p.getMensajeWhatsapp(),
                p.getPagadoCaja(),
                p.getActivo(),
                p.getFechaCreacion()
        );
    }

    private ProductoEcommerceResponse toEcommerceResponse(Producto p) {
        return new ProductoEcommerceResponse(
                p.getId(),
                p.getNombre(),
                p.getMarca(),
                p.getModelo(),
                p.getPrecioVenta(),
                p.getEstado(),
                p.getCategoria(),
                fromJson(p.getImagenes()),
                p.getDescripcion(),
                fromJson(p.getEspecificaciones()),
                p.getMensajeWhatsapp()
        );
    }

    private String toJson(List<String> list) {
        try {
            return list == null ? "[]" : objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }

    private void deleteImageFile(String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.out.println("No se pudo eliminar el archivo: " + filename + " - " + e.getMessage());
        }
    }

    private List<String> fromJson(String json) {
        try {
            if (json == null || json.isBlank()) return new ArrayList<>();
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
