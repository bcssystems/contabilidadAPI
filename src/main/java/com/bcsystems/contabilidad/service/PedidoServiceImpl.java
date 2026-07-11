package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.*;
import com.bcsystems.contabilidad.domain.en.EstadoPedido;
import com.bcsystems.contabilidad.domain.en.TipoMovimiento;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final PedidoDetalleRepository pedidoDetalleRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoStockRepository movimientoStockRepository;
    private final InventarioSucursalRepository inventarioSucursalRepository;

    public PedidoServiceImpl(PedidoRepository pedidoRepository,
                             PedidoDetalleRepository pedidoDetalleRepository,
                             ProductoRepository productoRepository,
                             MovimientoStockRepository movimientoStockRepository,
                             InventarioSucursalRepository inventarioSucursalRepository) {
        this.pedidoRepository = pedidoRepository;
        this.pedidoDetalleRepository = pedidoDetalleRepository;
        this.productoRepository = productoRepository;
        this.movimientoStockRepository = movimientoStockRepository;
        this.inventarioSucursalRepository = inventarioSucursalRepository;
    }

    @Override
    public Page<Pedido> buscarConFiltros(String search, String estado, Long idProveedor, Pageable pageable) {
        EstadoPedido estadoEnum = estado != null ? EstadoPedido.valueOf(estado) : null;
        return pedidoRepository.buscarConFiltros(search, estadoEnum, idProveedor, pageable);
    }

    @Override
    public Pedido findById(Integer id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pedido no encontrado"));
    }

    @Override
    @Transactional
    public Pedido save(Pedido pedido) {
        if (pedido.getFolio() == null || pedido.getFolio().isBlank()) {
            pedido.setFolio("PED-" + System.currentTimeMillis());
        }
        pedido.setEstado(EstadoPedido.PENDIENTE);
        for (PedidoDetalle detalle : pedido.getDetalles()) {
            detalle.setPedido(pedido);
        }
        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public Pedido recibir(Integer id, List<Map<String, Object>> detalles) {
        Pedido pedido = findById(id);

        for (Map<String, Object> d : detalles) {
            Integer idDetalle = Integer.valueOf(d.get("idPedidoDetalle").toString());
            Integer cantidadRecibida = Integer.valueOf(d.get("cantidadRecibida").toString());
            Double precioCompraUnitario = d.get("precioCompraUnitario") != null
                    ? Double.valueOf(d.get("precioCompraUnitario").toString()) : null;
            Double precioVentaSugerido = d.get("precioVentaSugerido") != null
                    ? Double.valueOf(d.get("precioVentaSugerido").toString()) : null;

            PedidoDetalle detalle = pedidoDetalleRepository.findById(idDetalle)
                    .orElseThrow(() -> new NotFoundException("Detalle de pedido no encontrado"));
            detalle.setCantidadRecibida(cantidadRecibida);
            if (precioCompraUnitario != null) detalle.setPrecioCompraUnitario(precioCompraUnitario);
            if (precioVentaSugerido != null) detalle.setCostoUltimo(precioVentaSugerido);
            pedidoDetalleRepository.save(detalle);

            Producto producto = detalle.getProducto();
            if (producto != null && cantidadRecibida > 0) {
                producto.setStockActual(producto.getStockActual() + cantidadRecibida);
                if (precioCompraUnitario != null) {
                    producto.setCostoPromedio(precioCompraUnitario);
                }
                productoRepository.save(producto);

                Integer idSucursal = pedido.getSucursal().getIdSucursal();
                InventarioSucursal inv = inventarioSucursalRepository
                        .findByProductoIdProductoAndSucursalIdSucursal(producto.getIdProducto(), idSucursal)
                        .orElse(null);
                if (inv != null) {
                    inv.setStock(inv.getStock() + cantidadRecibida);
                    inventarioSucursalRepository.save(inv);
                }

                MovimientoStock mov = MovimientoStock.builder()
                        .producto(producto)
                        .sucursal(pedido.getSucursal())
                        .tipoMovimiento(TipoMovimiento.ENTRADA)
                        .cantidad(cantidadRecibida)
                        .stockAnterior(producto.getStockActual() - cantidadRecibida)
                        .stockNuevo(producto.getStockActual())
                        .referencia("Pedido: " + pedido.getFolio())
                        .build();
                movimientoStockRepository.save(mov);
            }
        }

        boolean allRecibidas = pedido.getDetalles().stream()
                .allMatch(d -> d.getCantidadRecibida() != null && d.getCantidadRecibida() >= d.getCantidadPedida());
        boolean algunaRecibida = pedido.getDetalles().stream()
                .anyMatch(d -> d.getCantidadRecibida() != null && d.getCantidadRecibida() > 0);

        if (allRecibidas) {
            pedido.setEstado(EstadoPedido.COMPLETADO);
        } else if (algunaRecibida) {
            pedido.setEstado(EstadoPedido.PARCIAL);
        }

        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public Pedido cancelar(Integer id) {
        Pedido pedido = findById(id);
        pedido.setEstado(EstadoPedido.CANCELADO);
        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public Pedido completar(Integer id) {
        Pedido pedido = findById(id);
        pedido.setEstado(EstadoPedido.COMPLETADO);
        return pedidoRepository.save(pedido);
    }
}
