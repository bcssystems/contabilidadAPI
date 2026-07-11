package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.*;
import com.bcsystems.contabilidad.domain.en.TipoMovimiento;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.*;
import org.springframework.data.domain.Page;
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
    public Producto save(Producto producto) {
        if (producto.getStockActual() == null) producto.setStockActual(0);
        producto.setActivo(true);
        producto = productoRepository.save(producto);

        if (producto.getInventarioSucursales() != null) {
            for (InventarioSucursal inv : producto.getInventarioSucursales()) {
                inv.setProducto(producto);
                inventarioSucursalRepository.save(inv);
            }
        }

        return producto;
    }

    @Override
    public Page<Producto> buscarConFiltros(String search, Boolean activo, Boolean tieneVariantes, Pageable pageable) {
        return productoRepository.buscarConFiltros(search, activo, tieneVariantes, pageable);
    }

    @Override
    public Producto findById(Integer id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));
    }

    @Override
    public Page<Producto> findBySucursal(Integer idSucursal, Pageable pageable) {
        return productoRepository.findByActivoAndSucursal(idSucursal, pageable);
    }

    @Override
    @Transactional
    public Producto update(Integer id, Producto producto) {
        Producto existing = findById(id);
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

        return productoRepository.save(existing);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        Producto producto = findById(id);
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
    public Producto registrarMovimientoStock(Integer idProducto, String tipoMovimiento, Integer cantidad, Integer idSucursal, String referencia, String observacion) {
        Producto producto = findById(idProducto);
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

        return producto;
    }

    @Override
    @Transactional
    public Producto transferirStock(Integer idProducto, Integer idSucursalOrigen, Integer idSucursalDestino, Integer cantidad, String referencia, String observacion) {
        Producto producto = findById(idProducto);

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

        return producto;
    }
}
