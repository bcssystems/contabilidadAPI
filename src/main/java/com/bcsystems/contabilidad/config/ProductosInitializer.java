package com.bcsystems.contabilidad.config;

import com.bcsystems.contabilidad.domain.Producto;
import com.bcsystems.contabilidad.domain.en.CategoriaProducto;
import com.bcsystems.contabilidad.domain.en.EstadoProducto;
import com.bcsystems.contabilidad.repository.ProductoRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

            for (Map<String, Object> data : productosData) {
                Producto producto = Producto.builder()
                        .nombre((String) data.get("nombre"))
                        .marca((String) data.get("marca"))
                        .modelo((String) data.get("modelo"))
                        .precioCompra(new BigDecimal(data.get("precioCompra").toString()))
                        .precioVenta(new BigDecimal(data.get("precioVenta").toString()))
                        .estado(EstadoProducto.valueOf((String) data.get("estado")))
                        .categoria(CategoriaProducto.valueOf((String) data.get("categoria")))
                        .imagenes(objectMapper.writeValueAsString(data.get("imagenes")))
                        .descripcion((String) data.get("descripcion"))
                        .especificaciones(objectMapper.writeValueAsString(data.get("especificaciones")))
                        .mensajeWhatsapp((String) data.get("mensajeWhatsapp"))
                        .pagadoCaja(false)
                        .build();
                productoRepository.save(producto);
            }

            System.out.println("Productos iniciales insertados: " + productosData.size());
        } catch (Exception e) {
            System.out.println("No se pudieron cargar productos iniciales: " + e.getMessage());
        }
    }
}
