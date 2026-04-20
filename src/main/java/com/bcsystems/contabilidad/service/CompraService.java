package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.AsientoContable;
import com.bcsystems.contabilidad.domain.Compra;
import com.bcsystems.contabilidad.domain.CuentaContable;
import com.bcsystems.contabilidad.domain.MovimientoContable;
import com.bcsystems.contabilidad.domain.Proveedor;
import com.bcsystems.contabilidad.domain.en.EstadoCompra;
import com.bcsystems.contabilidad.domain.en.TipoAsientoContable;
import com.bcsystems.contabilidad.domain.en.TipoCompra;
import com.bcsystems.contabilidad.dto.CompraRegisterRequest;
import com.bcsystems.contabilidad.dto.CompraResponse;
import com.bcsystems.contabilidad.dto.CompraUpdateRequest;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.AsientoContableRepository;
import com.bcsystems.contabilidad.repository.CompraRepository;
import com.bcsystems.contabilidad.repository.CuentaContableRepository;
import com.bcsystems.contabilidad.repository.ProveedorRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CompraService {

    private final CompraRepository compraRepository;
    private final ProveedorRepository proveedorRepository;
    private final AsientoContableRepository asientoContableRepository;
    private final CuentaContableRepository cuentaContableRepository;

    public CompraService(CompraRepository compraRepository,
                      ProveedorRepository proveedorRepository,
                      AsientoContableRepository asientoContableRepository,
                      CuentaContableRepository cuentaContableRepository) {
        this.compraRepository = compraRepository;
        this.proveedorRepository = proveedorRepository;
        this.asientoContableRepository = asientoContableRepository;
        this.cuentaContableRepository = cuentaContableRepository;
    }

    @Transactional
    public Compra save(CompraRegisterRequest request) {
        Optional<TipoCompra> tipoCompra = Optional.ofNullable(request.tipoCompra());

        Compra compra = Compra.builder()
                .fecha_compra(request.fecha_compra())
                .total_compra(request.total_compra())
                .estadoCompra(EstadoCompra.PENDIENTE)
                .tipoCompra(tipoCompra.orElse(TipoCompra.CONTADO))
                .build();

        if (request.proveedorId() != null) {
            Optional<Proveedor> proveedor = proveedorRepository.findById(request.proveedorId());
            if (proveedor.isPresent()) {
                compra.setProveedor(proveedor.get());
            }
        }

        if (request.asientoId() == null) {
            AsientoContable asiento = crearAsientoAutomaticoCompra(compra);
            compra.setAsientoContable(asiento);
        } else {
            Optional<AsientoContable> asiento = asientoContableRepository.findById(request.asientoId());
            if (asiento.isPresent()) {
                compra.setAsientoContable(asiento.get());
            }
        }

        return compraRepository.save(compra);
    }

    private AsientoContable crearAsientoAutomaticoCompra(Compra compra) {
        String codigoCuentaGasto = "5101";
        String codigoCuentaPago;

        if (compra.getTipoCompra() == TipoCompra.CONTADO) {
            codigoCuentaPago = "1101";
        } else {
            codigoCuentaPago = "21";
        }

        Optional<CuentaContable> cuentaGasto = cuentaContableRepository.findByCodigo(codigoCuentaGasto);
        Optional<CuentaContable> cuentaPago = cuentaContableRepository.findByCodigo(codigoCuentaPago);

        if (cuentaGasto.isEmpty() || cuentaPago.isEmpty()) {
            throw new NotFoundException("Cuentas contables no encontradas para compra automática");
        }

        List<MovimientoContable> movimientos = new ArrayList<>();

        MovimientoContable movDebe = MovimientoContable.builder()
                .debe(compra.getTotal_compra())
                .haber(BigDecimal.ZERO)
                .cuentaContable(cuentaGasto.get())
                .build();
        movimientos.add(movDebe);

        MovimientoContable movHaber = MovimientoContable.builder()
                .debe(BigDecimal.ZERO)
                .haber(compra.getTotal_compra())
                .cuentaContable(cuentaPago.get())
                .build();
        movimientos.add(movHaber);

        String referencia = "Compra-" + (compra.getTipoCompra() == TipoCompra.CONTADO ? "Contado" : "Credito") + "-" + LocalDate.now();
        String descripcion = "Compra registrada el " + compra.getFecha_compra() + " por total de " + compra.getTotal_compra();

        AsientoContable asiento = AsientoContable.builder()
                .fecha(compra.getFecha_compra())
                .referencia(referencia)
                .descripcion(descripcion)
                .tipoAsientoContable(TipoAsientoContable.CONFIRMADO)
                .movimientos(new ArrayList<>())
                .build();

        asiento = asientoContableRepository.save(asiento);

        for (MovimientoContable mov : movimientos) {
            mov.setAsientoContable(asiento);
        }
        asiento.setMovimientos(movimientos);

        return asientoContableRepository.save(asiento);
    }

    public List<Compra> findAll() {
        return compraRepository.findAll();
    }

    public Page<CompraResponse> findAllPageable(PageRequest pageRequest) {
        Page<Compra> compras = compraRepository.findAll(pageRequest);
        return compras.map(this::toResponse);
    }

    private CompraResponse toResponse(Compra compra) {
        return new CompraResponse(
                compra.getIdCompra(),
                compra.getFecha_compra(),
                compra.getTotal_compra(),
                compra.getEstadoCompra(),
                compra.getProveedor() != null ? compra.getProveedor().getIdProveedor() : null,
                compra.getAsientoContable() != null ? compra.getAsientoContable().getIdAsientoContable() : null,
                compra.getTipoCompra()
        );
    }

    public Optional<Compra> findById(Long id) {
        Optional<Compra> compra = compraRepository.findById(id);
        if (compra.isPresent()) return compra;
        throw new NotFoundException("Compra no encontrada");
    }

    @Transactional
    public Compra update(Long id, CompraUpdateRequest request) {
        Optional<Compra> compraOpt = compraRepository.findById(id);
        if (!compraOpt.isPresent()) {
            throw new NotFoundException("Compra no encontrada");
        }

        Compra compra = compraOpt.get();

        if (request.fecha_compra() != null) {
            compra.setFecha_compra(request.fecha_compra());
        }
        if (request.total_compra() != null) {
            compra.setTotal_compra(request.total_compra());
        }
        if (request.estadoCompra() != null) {
            compra.setEstadoCompra(request.estadoCompra());
        }
        if (request.proveedorId() != null) {
            Optional<Proveedor> proveedor = proveedorRepository.findById(request.proveedorId());
            if (proveedor.isPresent()) {
                compra.setProveedor(proveedor.get());
            }
        }
        if (request.asientoId() != null) {
            Optional<AsientoContable> asiento = asientoContableRepository.findById(request.asientoId());
            if (asiento.isPresent()) {
                compra.setAsientoContable(asiento.get());
            }
        }

        return compraRepository.save(compra);
    }

    @Transactional
    public void delete(Long id) {
        Optional<Compra> compraOpt = compraRepository.findById(id);
        if (!compraOpt.isPresent()) {
            throw new NotFoundException("Compra no encontrada");
        }
        compraRepository.delete(compraOpt.get());
    }
}