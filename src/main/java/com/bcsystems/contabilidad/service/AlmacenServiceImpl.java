package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.MovimientoAlmacen;
import com.bcsystems.contabilidad.domain.Sucursal;
import com.bcsystems.contabilidad.domain.Usuario;
import com.bcsystems.contabilidad.domain.Variante;
import com.bcsystems.contabilidad.domain.en.EstadoMovimiento;
import com.bcsystems.contabilidad.domain.en.StatusProducto;
import com.bcsystems.contabilidad.domain.en.TipoMovimiento;
import com.bcsystems.contabilidad.dto.MovimientoAlmacenRegisterRequest;
import com.bcsystems.contabilidad.dto.MovimientoAlmacenResponse;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.MovimientoAlmacenRepository;
import com.bcsystems.contabilidad.repository.SucursalRepository;
import com.bcsystems.contabilidad.repository.UsuarioRepository;
import com.bcsystems.contabilidad.repository.VarianteRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class AlmacenServiceImpl implements AlmacenService {

    private final MovimientoAlmacenRepository movimientoAlmacenRepository;
    private final VarianteRepository varianteRepository;
    private final UsuarioRepository usuarioRepository;
    private final SucursalRepository sucursalRepository;

    public AlmacenServiceImpl(MovimientoAlmacenRepository movimientoAlmacenRepository,
                              VarianteRepository varianteRepository,
                              UsuarioRepository usuarioRepository,
                              SucursalRepository sucursalRepository) {
        this.movimientoAlmacenRepository = movimientoAlmacenRepository;
        this.varianteRepository = varianteRepository;
        this.usuarioRepository = usuarioRepository;
        this.sucursalRepository = sucursalRepository;
    }

    @Override
    @Transactional
    public MovimientoAlmacenResponse registrarEntrada(MovimientoAlmacenRegisterRequest request, Integer usuarioId) {
        Variante variante = varianteRepository.findById(request.varianteId())
                .orElseThrow(() -> new NotFoundException("Variante no encontrada"));

        Usuario solicitante = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        BigDecimal precio = request.precioCompra() != null
                ? request.precioCompra()
                : variante.getPrecioCompra();

        Sucursal sucursal = null;
        if (request.sucursalId() != null) {
            sucursal = sucursalRepository.findById(request.sucursalId())
                    .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));
        }

        MovimientoAlmacen movimiento = MovimientoAlmacen.builder()
                .tipo(TipoMovimiento.ENTRADA)
                .variante(variante)
                .cantidad(request.cantidad() != null ? request.cantidad() : 1)
                .solicitante(solicitante)
                .aprobador(solicitante)
                .fechaSolicitud(LocalDateTime.now())
                .fechaAprobacion(LocalDateTime.now())
                .fechaMovimiento(LocalDateTime.now())
                .estado(EstadoMovimiento.APROBADO)
                .observacion(request.observacion())
                .valeFolio(variante.getValeFolioSalida())
                .folioVenta(variante.getFolioVenta())
                .medioCompra(request.medioCompra())
                .proveedorNombre(variante.getProveedor() != null ? variante.getProveedor().getNombre() : null)
                .precioCompra(precio)
                .imei(variante.getImei())
                .descripcion(variante.getNombre() + " - " + variante.getProductoPadre().getNombre())
                .sucursal(sucursal)
                .build();

        movimiento = movimientoAlmacenRepository.save(movimiento);
        return toResponse(movimiento);
    }

    @Override
    @Transactional
    public MovimientoAlmacenResponse solicitarSalida(MovimientoAlmacenRegisterRequest request, Integer usuarioId) {
        Variante variante = varianteRepository.findById(request.varianteId())
                .orElseThrow(() -> new NotFoundException("Variante no encontrada"));

        if (variante.getStatus() != StatusProducto.DISPONIBLE) {
            throw new IllegalStateException("La variante no esta disponible para salida");
        }

        Usuario solicitante = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        BigDecimal precio = request.precioCompra() != null
                ? request.precioCompra()
                : variante.getPrecioCompra();

        Sucursal sucursal = null;
        if (request.sucursalId() != null) {
            sucursal = sucursalRepository.findById(request.sucursalId())
                    .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));
        }

        MovimientoAlmacen movimiento = MovimientoAlmacen.builder()
                .tipo(TipoMovimiento.SALIDA)
                .variante(variante)
                .cantidad(request.cantidad() != null ? request.cantidad() : 1)
                .solicitante(solicitante)
                .fechaSolicitud(LocalDateTime.now())
                .estado(EstadoMovimiento.PENDIENTE)
                .observacion(request.observacion())
                .valeFolio(request.valeFolio())
                .folioVenta(request.folioVenta())
                .medioCompra(request.medioCompra())
                .proveedorNombre(variante.getProveedor() != null ? variante.getProveedor().getNombre() : null)
                .precioCompra(precio)
                .imei(variante.getImei())
                .descripcion(variante.getNombre() + " - " + variante.getProductoPadre().getNombre())
                .sucursal(sucursal)
                .build();

        movimiento = movimientoAlmacenRepository.save(movimiento);
        return toResponse(movimiento);
    }

    @Override
    @Transactional
    public MovimientoAlmacenResponse aprobarSalida(Long movimientoId, Integer aprobadorId) {
        MovimientoAlmacen movimiento = movimientoAlmacenRepository.findById(movimientoId)
                .orElseThrow(() -> new NotFoundException("Movimiento no encontrado"));

        if (movimiento.getEstado() != EstadoMovimiento.PENDIENTE) {
            throw new IllegalStateException("El movimiento no esta pendiente");
        }

        Usuario aprobador = usuarioRepository.findById(aprobadorId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        Variante variante = movimiento.getVariante();
        variante.setStatus(StatusProducto.VENDIDO);
        variante.setValeFolioSalida(movimiento.getValeFolio());
        variante.setFolioVenta(movimiento.getFolioVenta());
        varianteRepository.save(variante);

        movimiento.setAprobador(aprobador);
        movimiento.setEstado(EstadoMovimiento.APROBADO);
        movimiento.setFechaAprobacion(LocalDateTime.now());
        movimiento.setFechaMovimiento(LocalDateTime.now());

        movimiento = movimientoAlmacenRepository.save(movimiento);
        return toResponse(movimiento);
    }

    @Override
    @Transactional
    public MovimientoAlmacenResponse rechazarSalida(Long movimientoId, Integer aprobadorId) {
        MovimientoAlmacen movimiento = movimientoAlmacenRepository.findById(movimientoId)
                .orElseThrow(() -> new NotFoundException("Movimiento no encontrado"));

        if (movimiento.getEstado() != EstadoMovimiento.PENDIENTE) {
            throw new IllegalStateException("El movimiento no esta pendiente");
        }

        Usuario aprobador = usuarioRepository.findById(aprobadorId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        movimiento.setAprobador(aprobador);
        movimiento.setEstado(EstadoMovimiento.RECHAZADO);
        movimiento.setFechaAprobacion(LocalDateTime.now());

        movimiento = movimientoAlmacenRepository.save(movimiento);
        return toResponse(movimiento);
    }

    @Override
    public Page<MovimientoAlmacenResponse> findPendientes(PageRequest pageRequest) {
        Page<MovimientoAlmacen> movimientos = movimientoAlmacenRepository
                .findByTipoAndEstadoOrderByFechaSolicitudAsc(
                        TipoMovimiento.SALIDA, EstadoMovimiento.PENDIENTE, pageRequest);
        return movimientos.map(this::toResponse);
    }

    @Override
    public Page<MovimientoAlmacenResponse> findAllMovimientos(PageRequest pageRequest) {
        Page<MovimientoAlmacen> movimientos = movimientoAlmacenRepository
                .findAll(pageRequest);
        return movimientos.map(this::toResponse);
    }

    @Override
    public MovimientoAlmacenResponse findById(Long id) {
        MovimientoAlmacen movimiento = movimientoAlmacenRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Movimiento no encontrado"));
        return toResponse(movimiento);
    }

    @Override
    public byte[] generarPdfSalida(Long movimientoId) {
        MovimientoAlmacen movimiento = movimientoAlmacenRepository.findById(movimientoId)
                .orElseThrow(() -> new NotFoundException("Movimiento no encontrado"));

        return PdfGeneratorService.generarPdfSalida(movimiento);
    }

    private MovimientoAlmacenResponse toResponse(MovimientoAlmacen m) {
        String solicitanteNombre = null;
        if (m.getSolicitante() != null) {
            solicitanteNombre = m.getSolicitante().getPersona() != null
                    ? m.getSolicitante().getPersona().getNombre() + " " + m.getSolicitante().getPersona().getPrimerApellido()
                    : m.getSolicitante().getUsername();
        }

        String aprobadorNombre = null;
        if (m.getAprobador() != null) {
            aprobadorNombre = m.getAprobador().getPersona() != null
                    ? m.getAprobador().getPersona().getNombre() + " " + m.getAprobador().getPersona().getPrimerApellido()
                    : m.getAprobador().getUsername();
        }

        return new MovimientoAlmacenResponse(
                m.getId(),
                m.getTipo(),
                m.getVariante().getId(),
                m.getVariante().getNombre(),
                m.getVariante().getNumeroSerie(),
                m.getVariante().getProductoPadre().getNombre(),
                m.getVariante().getProductoPadre().getSku(),
                m.getCantidad(),
                m.getSolicitante().getIdUser(),
                solicitanteNombre,
                m.getAprobador() != null ? m.getAprobador().getIdUser() : null,
                aprobadorNombre,
                m.getFechaSolicitud(),
                m.getFechaAprobacion(),
                m.getFechaMovimiento(),
                m.getEstado(),
                m.getObservacion(),
                m.getValeFolio(),
                m.getFolioVenta(),
                m.getMedioCompra(),
                m.getProveedorNombre(),
                m.getPrecioCompra(),
                m.getImei(),
                m.getDescripcion(),
                m.getSucursal() != null ? m.getSucursal().getIdSucursal() : null,
                m.getSucursal() != null ? m.getSucursal().getNombre() : null
        );
    }
}
