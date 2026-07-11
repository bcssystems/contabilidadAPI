package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.*;
import com.bcsystems.contabilidad.domain.en.EstadoVenta;
import com.bcsystems.contabilidad.domain.en.TipoMovimientoCaja;
import com.bcsystems.contabilidad.domain.en.TipoVenta;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CorteCajaServiceImpl implements CorteCajaService {

    private final CorteCajaRepository corteCajaRepository;
    private final CorteDetallePagoRepository corteDetallePagoRepository;
    private final CajaRepository cajaRepository;
    private final VentaRepository ventaRepository;
    private final MovimientoCajaRepository movimientoCajaRepository;
    private final TipoPagoRepository tipoPagoRepository;
    private final VentaPagoRepository ventaPagoRepository;
    private final PersonaRepository personaRepository;

    public CorteCajaServiceImpl(CorteCajaRepository corteCajaRepository,
                                CorteDetallePagoRepository corteDetallePagoRepository,
                                CajaRepository cajaRepository,
                                VentaRepository ventaRepository,
                                MovimientoCajaRepository movimientoCajaRepository,
                                TipoPagoRepository tipoPagoRepository,
                                VentaPagoRepository ventaPagoRepository,
                                PersonaRepository personaRepository) {
        this.corteCajaRepository = corteCajaRepository;
        this.corteDetallePagoRepository = corteDetallePagoRepository;
        this.cajaRepository = cajaRepository;
        this.ventaRepository = ventaRepository;
        this.movimientoCajaRepository = movimientoCajaRepository;
        this.tipoPagoRepository = tipoPagoRepository;
        this.ventaPagoRepository = ventaPagoRepository;
        this.personaRepository = personaRepository;
    }

    @Override
    public Page<CorteCaja> buscarConFiltros(Integer idSucursal, Integer idCaja, LocalDateTime fechaInicio, LocalDateTime fechaFin, Pageable pageable) {
        return corteCajaRepository.buscarConFiltros(idSucursal, idCaja, fechaInicio, fechaFin, pageable);
    }

    @Override
    public CorteCaja findById(Integer id) {
        return corteCajaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Corte no encontrado"));
    }

    @Override
    @Transactional
    public CorteCaja realizarCorte(Integer idCaja, Integer idUsuario) {
        Caja caja = cajaRepository.findById(idCaja)
                .orElseThrow(() -> new NotFoundException("Caja no encontrada"));

        LocalDateTime apertura = caja.getFechaApertura();
        LocalDateTime ahora = LocalDateTime.now();

        List<Venta> ventas = ventaRepository.findByCajaIdCajaAndFechaBetween(idCaja, apertura, ahora);

        double totalVentas = ventas.stream().mapToDouble(Venta::getTotal).sum();
        double totalContado = ventas.stream()
                .filter(v -> v.getTipoVenta() == TipoVenta.CONTADO && v.getEstado() == EstadoVenta.COMPLETADA)
                .mapToDouble(Venta::getTotal).sum();
        double totalCredito = ventas.stream()
                .filter(v -> v.getTipoVenta() == TipoVenta.CREDITO && v.getEstado() == EstadoVenta.COMPLETADA)
                .mapToDouble(Venta::getTotal).sum();

        double totalIngresos = Optional.ofNullable(
                movimientoCajaRepository.sumMontoByCajaIdCajaAndTipoAndFechaBetween(
                        idCaja, TipoMovimientoCaja.INGRESO, apertura, ahora)
        ).orElse(0.0);

        double totalEgresos = Optional.ofNullable(
                movimientoCajaRepository.sumMontoByCajaIdCajaAndTipoAndFechaBetween(
                        idCaja, TipoMovimientoCaja.EGRESO, apertura, ahora)
        ).orElse(0.0);

        double saldoFinal = caja.getSaldoActual();

        Persona usuario = personaRepository.findById(idUsuario)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        CorteCaja corte = CorteCaja.builder()
                .caja(caja)
                .saldoInicial(caja.getSaldoActual() - totalVentas + totalEgresos - totalIngresos)
                .totalVentas(totalVentas)
                .totalVentasContado(totalContado)
                .totalVentasCredito(totalCredito)
                .totalIngresos(totalIngresos)
                .totalEgresos(totalEgresos)
                .saldoFinalContado(saldoFinal)
                .fechaApertura(apertura)
                .fechaCierre(ahora)
                .usuario(usuario)
                .build();

        corte = corteCajaRepository.save(corte);

        List<TipoPago> tiposPago = tipoPagoRepository.findByActivoTrue();
        for (TipoPago tp : tiposPago) {
            double montoTipo = ventaPagoRepository.findByVentaIdVentaIn(
                    ventas.stream().map(Venta::getIdVenta).toList()
            ).stream()
                    .filter(vp -> vp.getTipoPago().getIdTipoPago().equals(tp.getIdTipoPago()))
                    .mapToDouble(VentaPago::getMonto)
                    .sum();

            if (montoTipo > 0) {
                CorteDetallePago detalle = CorteDetallePago.builder()
                        .corte(corte)
                        .tipoPago(tp)
                        .monto(montoTipo)
                        .build();
                corteDetallePagoRepository.save(detalle);
            }
        }

        caja.setEstado(com.bcsystems.contabilidad.domain.en.CajaEstado.CERRADA);
        caja.setFechaCierre(ahora);
        cajaRepository.save(caja);

        return corte;
    }

    @Override
    public Map<String, Object> previewCorte(Integer idCaja) {
        Caja caja = cajaRepository.findById(idCaja)
                .orElseThrow(() -> new NotFoundException("Caja no encontrada"));

        LocalDateTime apertura = caja.getFechaApertura();
        LocalDateTime ahora = LocalDateTime.now();

        List<Venta> ventas = ventaRepository.findByCajaIdCajaAndFechaBetween(idCaja, apertura, ahora);

        double totalVentas = ventas.stream().mapToDouble(Venta::getTotal).sum();
        double totalContado = ventas.stream()
                .filter(v -> v.getTipoVenta() == TipoVenta.CONTADO && v.getEstado() == EstadoVenta.COMPLETADA)
                .mapToDouble(Venta::getTotal).sum();
        double totalCredito = ventas.stream()
                .filter(v -> v.getTipoVenta() == TipoVenta.CREDITO && v.getEstado() == EstadoVenta.COMPLETADA)
                .mapToDouble(Venta::getTotal).sum();

        double totalIngresos = Optional.ofNullable(
                movimientoCajaRepository.sumMontoByCajaIdCajaAndTipoAndFechaBetween(
                        idCaja, TipoMovimientoCaja.INGRESO, apertura, ahora)
        ).orElse(0.0);

        double totalEgresos = Optional.ofNullable(
                movimientoCajaRepository.sumMontoByCajaIdCajaAndTipoAndFechaBetween(
                        idCaja, TipoMovimientoCaja.EGRESO, apertura, ahora)
        ).orElse(0.0);

        Map<String, Object> preview = new HashMap<>();
        preview.put("caja", caja);
        preview.put("totalVentas", totalVentas);
        preview.put("totalVentasContado", totalContado);
        preview.put("totalVentasCredito", totalCredito);
        preview.put("totalIngresos", totalIngresos);
        preview.put("totalEgresos", totalEgresos);
        preview.put("ventasCount", ventas.size());
        return preview;
    }
}
