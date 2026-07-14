package com.bcsystems.contabilidad.config;

import com.bcsystems.contabilidad.domain.Producto;
import com.bcsystems.contabilidad.domain.en.CategoriaProducto;
import com.bcsystems.contabilidad.repository.ProductoRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ProductosInitializer implements CommandLineRunner {

    private final ProductoRepository productoRepository;
    private final ObjectMapper objectMapper;

    public ProductosInitializer(ProductoRepository productoRepository, ObjectMapper objectMapper) {
        this.productoRepository = productoRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) {
        if (productoRepository.count() > 0) {
            return;
        }

        try {
            InputStream input = new ClassPathResource("productos-initial.json").getInputStream();
            List<Map<String, Object>> productosData = objectMapper.readValue(input, new TypeReference<>() {});
            Set<String> usedSkus = new HashSet<>();

            for (Map<String, Object> data : productosData) {
                String nombre = (String) data.get("nombre");
                String baseSku = nombre.toUpperCase().replaceAll("[^A-Z0-9]", "_").replaceAll("_+", "_").replaceAll("^_|_$", "");
                if (baseSku.length() > 40) baseSku = baseSku.substring(0, 40);
                String sku = baseSku;
                int counter = 1;
                while (usedSkus.contains(sku)) {
                    sku = baseSku + "_" + counter++;
                }
                usedSkus.add(sku);

                Double precioVenta = data.get("precioVenta") != null ? ((Number) data.get("precioVenta")).doubleValue() : null;
                String categoriaStr = (String) data.get("categoria");
                CategoriaProducto categoria = null;
                if (categoriaStr != null) {
                    try { categoria = CategoriaProducto.valueOf(categoriaStr); }
                    catch (IllegalArgumentException e) { categoria = null; }
                }

                Producto producto = Producto.builder()
                        .sku(sku)
                        .nombre(nombre)
                        .descripcion((String) data.get("descripcion"))
                        .precio1(precioVenta)
                        .categoria(categoria)
                        .stockActual(0)
                        .activo(true)
                        .build();
                productoRepository.save(producto);
            }

            System.out.println("Productos iniciales insertados: " + productosData.size());
        } catch (Exception e) {
            System.out.println("No se pudieron cargar productos iniciales: " + e.getMessage());
        }
    }
}
