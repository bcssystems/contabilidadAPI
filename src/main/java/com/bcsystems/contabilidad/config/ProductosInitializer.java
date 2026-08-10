package com.bcsystems.contabilidad.config;

import com.bcsystems.contabilidad.domain.Caja;
import com.bcsystems.contabilidad.domain.InventarioSucursal;
import com.bcsystems.contabilidad.domain.Producto;
import com.bcsystems.contabilidad.domain.Sucursal;
import com.bcsystems.contabilidad.domain.en.CajaEstado;
import com.bcsystems.contabilidad.domain.en.CategoriaProducto;
import com.bcsystems.contabilidad.repository.CajaRepository;
import com.bcsystems.contabilidad.repository.InventarioSucursalRepository;
import com.bcsystems.contabilidad.repository.ProductoRepository;
import com.bcsystems.contabilidad.repository.SucursalRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class ProductosInitializer implements CommandLineRunner {

    private static final String SUCURSAL_NOMBRE = "Sucursal Principal";
    private static final String CAJA_NOMBRE = "Caja Principal";

    private final ProductoRepository productoRepository;
    private final SucursalRepository sucursalRepository;
    private final CajaRepository cajaRepository;
    private final InventarioSucursalRepository inventarioSucursalRepository;
    private final ObjectMapper objectMapper;

    public ProductosInitializer(ProductoRepository productoRepository,
                                SucursalRepository sucursalRepository,
                                CajaRepository cajaRepository,
                                InventarioSucursalRepository inventarioSucursalRepository,
                                ObjectMapper objectMapper) {
        this.productoRepository = productoRepository;
        this.sucursalRepository = sucursalRepository;
        this.cajaRepository = cajaRepository;
        this.inventarioSucursalRepository = inventarioSucursalRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Sucursal sucursal = obtenerOCrearSucursal();
        obtenerOCrearCaja(sucursal);

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
                        .marca((String) data.get("marca"))
                        .modelo((String) data.get("modelo"))
                        .precio1(precioVenta)
                        .categoria(categoria)
                        .stockActual(1)
                        .stockMinimo(0)
                        .stockMaximo(1)
                        .activo(true)
                        .build();
                producto = productoRepository.save(producto);

                inventarioSucursalRepository.save(InventarioSucursal.builder()
                        .producto(producto)
                        .sucursal(sucursal)
                        .stock(1)
                        .stockMinimo(0)
                        .stockMaximo(1)
                        .build());
            }

            System.out.println("Productos iniciales insertados: " + productosData.size() + " en sucursal " + sucursal.getIdSucursal());
        } catch (Exception e) {
            System.out.println("No se pudieron cargar productos iniciales: " + e.getMessage());
        }
    }

    private Sucursal obtenerOCrearSucursal() {
        return sucursalRepository.findByActivaTrueOrderByNombreAsc().stream()
                .filter(s -> SUCURSAL_NOMBRE.equalsIgnoreCase(s.getNombre()))
                .findFirst()
                .orElseGet(() -> sucursalRepository.save(
                        Sucursal.builder()
                                .nombre(SUCURSAL_NOMBRE)
                                .activa(true)
                                .build()));
    }

    private Caja obtenerOCrearCaja(Sucursal sucursal) {
        return cajaRepository.findBySucursalIdSucursalAndActivaTrue(sucursal.getIdSucursal()).stream()
                .findFirst()
                .orElseGet(() -> cajaRepository.save(
                        Caja.builder()
                                .nombre(CAJA_NOMBRE)
                                .sucursal(sucursal)
                                .estado(CajaEstado.CERRADA)
                                .saldoActual(0.0)
                                .activa(true)
                                .build()));
    }
}
