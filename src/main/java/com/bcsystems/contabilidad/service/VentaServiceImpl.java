package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.*;
import com.bcsystems.contabilidad.domain.en.EstadoVenta;
import com.bcsystems.contabilidad.domain.en.TipoMovimiento;
import com.bcsystems.contabilidad.domain.en.TipoVenta;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final VentaDetalleRepository ventaDetalleRepository;
    private final VentaPagoRepository ventaPagoRepository;
    private final ProductoRepository productoRepository;
    private final CajaRepository cajaRepository;
    private final ClienteRepository clienteRepository;
    private final PersonaRepository personaRepository;
    private final MovimientoStockRepository movimientoStockRepository;
    private final InventarioSucursalRepository inventarioSucursalRepository;

    public VentaServiceImpl(VentaRepository ventaRepository,
                            VentaDetalleRepository ventaDetalleRepository,
                            VentaPagoRepository ventaPagoRepository,
                            ProductoRepository productoRepository,
                            CajaRepository cajaRepository,
                            ClienteRepository clienteRepository,
                            PersonaRepository personaRepository,
                            MovimientoStockRepository movimientoStockRepository,
                            InventarioSucursalRepository inventarioSucursalRepository) {
        this.ventaRepository = ventaRepository;
        this.ventaDetalleRepository = ventaDetalleRepository;
        this.ventaPagoRepository = ventaPagoRepository;
        this.productoRepository = productoRepository;
        this.cajaRepository = cajaRepository;
        this.clienteRepository = clienteRepository;
        this.personaRepository = personaRepository;
        this.movimientoStockRepository = movimientoStockRepository;
        this.inventarioSucursalRepository = inventarioSucursalRepository;
    }

    @Override
    @Transactional
    public Venta save(Venta venta, List<VentaDetalle> detalles, List<VentaPago> pagos) {
        venta.setFecha(LocalDateTime.now());
        venta.setEstado(EstadoVenta.COMPLETADA);

        if (venta.getCaja() != null && venta.getCaja().getIdCaja() != null) {
            Caja caja = cajaRepository.findById(venta.getCaja().getIdCaja())
                    .orElseThrow(() -> new NotFoundException("Caja no encontrada"));
            venta.setCaja(caja);
        }

        if (venta.getCliente() != null && venta.getCliente().getIdCliente() != null) {
            Cliente cliente = clienteRepository.findById(venta.getCliente().getIdCliente())
                    .orElse(null);
            venta.setCliente(cliente);
        }

        venta = ventaRepository.save(venta);

        if (detalles != null) {
            for (VentaDetalle detalle : detalles) {
                detalle.setVenta(venta);
                ventaDetalleRepository.save(detalle);

                if (detalle.getProducto() != null && detalle.getProducto().getIdProducto() != null) {
                    Producto producto = productoRepository.findById(detalle.getProducto().getIdProducto()).orElse(null);
                    if (producto != null) {
                        int stockAnterior = producto.getStockActual();
                        producto.setStockActual(stockAnterior - detalle.getCantidad());
                        productoRepository.save(producto);

                        MovimientoStock mov = MovimientoStock.builder()
                                .producto(producto)
                                .tipoMovimiento(TipoMovimiento.SALIDA)
                                .cantidad(detalle.getCantidad())
                                .stockAnterior(stockAnterior)
                                .stockNuevo(producto.getStockActual())
                                .referencia("Venta #" + venta.getIdVenta())
                                .build();
                        movimientoStockRepository.save(mov);

                        if (venta.getCaja() != null && venta.getCaja().getSucursal() != null) {
                            Integer idSucursal = venta.getCaja().getSucursal().getIdSucursal();
                            InventarioSucursal inv = inventarioSucursalRepository
                                    .findByProductoIdProductoAndSucursalIdSucursal(producto.getIdProducto(), idSucursal)
                                    .orElse(null);
                            if (inv != null) {
                                inv.setStock(inv.getStock() - detalle.getCantidad());
                                inventarioSucursalRepository.save(inv);
                            }
                        }
                    }
                }
            }
        }

        if (pagos != null) {
            for (VentaPago pago : pagos) {
                pago.setVenta(venta);
                ventaPagoRepository.save(pago);
            }
        }

        return venta;
    }

    @Override
    public List<Venta> findAll() {
        return ventaRepository.findAll();
    }

    @Override
    public Page<Venta> buscarConFiltros(Integer idSucursal, Integer idCaja, EstadoVenta estado, LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable) {
        return ventaRepository.buscarConFiltros(idSucursal, idCaja, estado, fechaInicio, fechaFin, pageable);
    }

    @Override
    public Optional<Venta> findById(Integer id) {
        Optional<Venta> venta = ventaRepository.findById(id);
        if (venta.isEmpty()) {
            throw new NotFoundException("Venta no encontrada");
        }
        return venta;
    }

    @Override
    public List<Venta> findByCaja(Integer idCaja) {
        return ventaRepository.findByCajaIdCaja(idCaja);
    }

    @Override
    @Transactional
    public Venta cancelar(Integer id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Venta no encontrada"));
        venta.setEstado(EstadoVenta.CANCELADA);
        return ventaRepository.save(venta);
    }
}
