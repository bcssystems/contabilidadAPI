package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.*;
import com.bcsystems.contabilidad.domain.en.EstadoVenta;
import com.bcsystems.contabilidad.domain.en.TipoMovimiento;
import com.bcsystems.contabilidad.dto.VentaDetalleResponse;
import com.bcsystems.contabilidad.dto.VentaPagoResponse;
import com.bcsystems.contabilidad.dto.VentaResponse;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
    public VentaResponse save(Venta venta, List<VentaDetalle> detalles, List<VentaPago> pagos) {
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

        List<VentaDetalleResponse> detalleResponses = detalles != null
            ? detalles.stream().map(VentaDetalleResponse::fromEntity).toList()
            : List.of();
        List<VentaPagoResponse> pagoResponses = pagos != null
            ? pagos.stream().map(VentaPagoResponse::fromEntity).toList()
            : List.of();

        return VentaResponse.fromEntity(venta, detalleResponses, pagoResponses);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponse> findAll() {
        return ventaRepository.findAll().stream()
                .map(v -> VentaResponse.fromEntity(v, List.of(), List.of()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VentaResponse> buscarConFiltros(Integer idSucursal, Integer idCaja, EstadoVenta estado, LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable) {
        return ventaRepository.buscarConFiltros(idSucursal, idCaja, estado, fechaInicio, fechaFin, pageable)
                .map(v -> VentaResponse.fromEntity(v, List.of(), List.of()));
    }

    @Override
    @Transactional(readOnly = true)
    public VentaResponse findById(Integer id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Venta no encontrada"));
        List<VentaDetalleResponse> detalles = ventaDetalleRepository.findByVentaIdVenta(id).stream()
                .map(VentaDetalleResponse::fromEntity)
                .toList();
        List<VentaPagoResponse> pagos = ventaPagoRepository.findByVentaIdVenta(id).stream()
                .map(VentaPagoResponse::fromEntity)
                .toList();
        return VentaResponse.fromEntity(venta, detalles, pagos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VentaResponse> findByCaja(Integer idCaja) {
        return ventaRepository.findByCajaIdCaja(idCaja).stream()
                .map(v -> VentaResponse.fromEntity(v, List.of(), List.of()))
                .toList();
    }

    @Override
    @Transactional
    public VentaResponse cancelar(Integer id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Venta no encontrada"));
        venta.setEstado(EstadoVenta.CANCELADA);
        venta = ventaRepository.save(venta);
        List<VentaDetalleResponse> detalles = ventaDetalleRepository.findByVentaIdVenta(id).stream()
                .map(VentaDetalleResponse::fromEntity)
                .toList();
        List<VentaPagoResponse> pagos = ventaPagoRepository.findByVentaIdVenta(id).stream()
                .map(VentaPagoResponse::fromEntity)
                .toList();
        return VentaResponse.fromEntity(venta, detalles, pagos);
    }
}
