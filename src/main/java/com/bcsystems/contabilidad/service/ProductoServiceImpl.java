package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.*;
import com.bcsystems.contabilidad.domain.en.TipoMovimiento;
import com.bcsystems.contabilidad.dto.*;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final InventarioSucursalRepository inventarioSucursalRepository;
    private final MovimientoStockRepository movimientoStockRepository;
    private final SucursalRepository sucursalRepository;

    public ProductoServiceImpl(ProductoRepository productoRepository,
                               InventarioSucursalRepository inventarioSucursalRepository,
                               MovimientoStockRepository movimientoStockRepository,
                               SucursalRepository sucursalRepository) {
        this.productoRepository = productoRepository;
        this.inventarioSucursalRepository = inventarioSucursalRepository;
        this.movimientoStockRepository = movimientoStockRepository;
        this.sucursalRepository = sucursalRepository;
    }

    @Override
    @Transactional
    public ProductoResponse save(Producto producto) {
        if (producto.getStockActual() == null) producto.setStockActual(0);
        producto.setActivo(true);
        producto = productoRepository.save(producto);

        if (producto.getInventarioSucursales() != null) {
            for (InventarioSucursal inv : producto.getInventarioSucursales()) {
                inv.setProducto(producto);
                inventarioSucursalRepository.save(inv);
            }
        }

        return toResponse(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoResponse> buscarConFiltros(String search, Boolean activo, Boolean tieneVariantes, Pageable pageable) {
        return toResponsePage(productoRepository.buscarConFiltros(search, activo, tieneVariantes, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponse findById(Integer id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));
        return toResponse(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoResponse> findBySucursal(Integer idSucursal, Pageable pageable) {
        return toResponsePage(productoRepository.findByActivoAndSucursal(idSucursal, pageable));
    }

    @Override
    @Transactional
    public ProductoResponse update(Integer id, Producto producto) {
        Producto existing = productoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));
        if (producto.getSku() != null) existing.setSku(producto.getSku());
        if (producto.getNombre() != null) existing.setNombre(producto.getNombre());
        if (producto.getDescripcion() != null) existing.setDescripcion(producto.getDescripcion());
        if (producto.getPrecio1() != null) existing.setPrecio1(producto.getPrecio1());
        if (producto.getPrecio2() != null) existing.setPrecio2(producto.getPrecio2());
        if (producto.getPrecio3() != null) existing.setPrecio3(producto.getPrecio3());
        if (producto.getPrecio4() != null) existing.setPrecio4(producto.getPrecio4());
        if (producto.getPrecioPersonalizado() != null) existing.setPrecioPersonalizado(producto.getPrecioPersonalizado());
        if (producto.getStockActual() != null) existing.setStockActual(producto.getStockActual());
        if (producto.getStockMinimo() != null) existing.setStockMinimo(producto.getStockMinimo());
        if (producto.getStockMaximo() != null) existing.setStockMaximo(producto.getStockMaximo());
        if (producto.getCostoPromedio() != null) existing.setCostoPromedio(producto.getCostoPromedio());
        if (producto.getTieneVariantes() != null) existing.setTieneVariantes(producto.getTieneVariantes());
        if (producto.getActivo() != null) existing.setActivo(producto.getActivo());

        return toResponse(productoRepository.save(existing));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProductos", productoRepository.countByActivoTrue());
        stats.put("totalVariantes", productoRepository.findByTieneVariantesTrueAndActivoTrue().size());
        return stats;
    }

    @Override
    public ProductoMultimediaService getMultimediaService() {
        return null;
    }

    @Override
    @Transactional
    public ProductoResponse registrarMovimientoStock(Integer idProducto, String tipoMovimiento, Integer cantidad, Integer idSucursal, String referencia, String observacion) {
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));
        TipoMovimiento tipo = TipoMovimiento.valueOf(tipoMovimiento);

        int stockAnterior = producto.getStockActual();
        int stockNuevo = stockAnterior;

        switch (tipo) {
            case ENTRADA:
                stockNuevo = stockAnterior + cantidad;
                break;
            case SALIDA:
                stockNuevo = stockAnterior - cantidad;
                break;
            case AJUSTE:
                stockNuevo = cantidad;
                cantidad = stockNuevo - stockAnterior;
                break;
            default:
                break;
        }

        producto.setStockActual(stockNuevo);
        productoRepository.save(producto);

        Sucursal sucursal = idSucursal != null ? sucursalRepository.findById(idSucursal).orElse(null) : null;

        MovimientoStock mov = MovimientoStock.builder()
                .producto(producto)
                .sucursal(sucursal)
                .tipoMovimiento(tipo)
                .cantidad(Math.abs(cantidad))
                .stockAnterior(stockAnterior)
                .stockNuevo(stockNuevo)
                .referencia(referencia)
                .observacion(observacion)
                .build();
        movimientoStockRepository.save(mov);

        if (sucursal != null) {
            InventarioSucursal inv = inventarioSucursalRepository
                    .findByProductoIdProductoAndSucursalIdSucursal(idProducto, idSucursal)
                    .orElse(null);
            if (inv != null) {
                inv.setStock(stockNuevo);
                inventarioSucursalRepository.save(inv);
            }
        }

        return toResponse(producto);
    }

    @Override
    @Transactional
    public ProductoResponse transferirStock(Integer idProducto, Integer idSucursalOrigen, Integer idSucursalDestino, Integer cantidad, String referencia, String observacion) {
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));

        InventarioSucursal origen = inventarioSucursalRepository
                .findByProductoIdProductoAndSucursalIdSucursal(idProducto, idSucursalOrigen)
                .orElseThrow(() -> new NotFoundException("Inventario origen no encontrado"));

        InventarioSucursal destino = inventarioSucursalRepository
                .findByProductoIdProductoAndSucursalIdSucursal(idProducto, idSucursalDestino)
                .orElse(null);

        origen.setStock(origen.getStock() - cantidad);
        inventarioSucursalRepository.save(origen);

        if (destino != null) {
            destino.setStock(destino.getStock() + cantidad);
            inventarioSucursalRepository.save(destino);
        }

        Sucursal sucOrigen = sucursalRepository.findById(idSucursalOrigen).orElse(null);

        MovimientoStock mov = MovimientoStock.builder()
                .producto(producto)
                .sucursal(sucOrigen)
                .tipoMovimiento(TipoMovimiento.TRANSFERENCIA)
                .cantidad(cantidad)
                .stockAnterior(origen.getStock() + cantidad)
                .stockNuevo(origen.getStock())
                .referencia(referencia != null ? referencia : "Transferencia a sucursal " + idSucursalDestino)
                .observacion(observacion)
                .build();
        movimientoStockRepository.save(mov);

        return toResponse(producto);
    }

    private ProductoResponse toResponse(Producto p) {
        Map<Integer, List<Producto>> childrenByParent = new HashMap<>();
        if (Boolean.TRUE.equals(p.getTieneVariantes())) {
            childrenByParent.put(p.getIdProducto(), productoRepository.findByProductoPadreIdProducto(p.getIdProducto()));
        }
        return buildResponse(p, childrenByParent);
    }

    private Page<ProductoResponse> toResponsePage(Page<Producto> page) {
        List<Producto> content = page.getContent();
        Set<Integer> parentIds = new HashSet<>();
        for (Producto p : content) {
            if (Boolean.TRUE.equals(p.getTieneVariantes())) {
                parentIds.add(p.getIdProducto());
            }
        }
        Map<Integer, List<Producto>> childrenByParent = new HashMap<>();
        if (!parentIds.isEmpty()) {
            for (Producto child : productoRepository.findByProductoPadreIdProductoIn(parentIds)) {
                childrenByParent
                        .computeIfAbsent(child.getProductoPadre().getIdProducto(), k -> new ArrayList<>())
                        .add(child);
            }
        }
        List<ProductoResponse> dto = content.stream()
                .map(p -> buildResponse(p, childrenByParent))
                .toList();
        return new PageImpl<>(dto, page.getPageable(), page.getTotalElements());
    }

    private ProductoResponse buildResponse(Producto p, Map<Integer, List<Producto>> childrenByParent) {
        List<ProductoResponse> variantes = childrenByParent.getOrDefault(p.getIdProducto(), List.of())
                .stream()
                .map(c -> buildResponse(c, Map.of()))
                .toList();
        return new ProductoResponse(
                p.getIdProducto(),
                p.getSku(),
                p.getNombre(),
                p.getDescripcion(),
                p.getMarca(),
                p.getModelo(),
                p.getCategoria(),
                p.getPrecio1(),
                p.getPrecio2(),
                p.getPrecio3(),
                p.getPrecio4(),
                p.getPrecioPersonalizado(),
                p.getStockActual(),
                p.getStockMinimo(),
                p.getStockMaximo(),
                p.getCostoPromedio(),
                p.getTieneVariantes(),
                p.getProductoPadre() != null ? p.getProductoPadre().getIdProducto() : null,
                p.getProductoPadre() != null ? p.getProductoPadre().getNombre() : null,
                p.getActivo(),
                p.getFechaCreacion(),
                p.getFechaActualizacion(),
                p.getMultimedia().stream().map(ProductoMultimediaResponse::fromEntity).toList(),
                p.getInventarioSucursales().stream().map(this::toInventarioResponse).toList(),
                p.getVarianteAtributos().stream().map(this::toAtributoResponse).toList(),
                variantes
        );
    }

    private InventarioSucursalResponse toInventarioResponse(InventarioSucursal inv) {
        return new InventarioSucursalResponse(
                inv.getId(),
                inv.getSucursal() != null ? inv.getSucursal().getIdSucursal() : null,
                inv.getSucursal() != null ? inv.getSucursal().getNombre() : null,
                inv.getStock(),
                inv.getStockMinimo(),
                inv.getStockMaximo()
        );
    }

    private AtributoAsignadoResponse toAtributoResponse(ProductoVarianteAtributo pva) {
        return new AtributoAsignadoResponse(
                pva.getAtributo() != null ? pva.getAtributo().getIdAtributo() : null,
                pva.getAtributo() != null ? pva.getAtributo().getNombre() : null,
                pva.getValor() != null ? pva.getValor().getIdValor() : null,
                pva.getValor() != null ? pva.getValor().getValor() : null,
                pva.getValor() != null ? pva.getValor().getCodigoSku() : null
        );
    }
}
