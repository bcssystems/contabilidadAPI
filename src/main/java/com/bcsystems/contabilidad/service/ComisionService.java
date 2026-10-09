package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Comision;
import com.bcsystems.contabilidad.domain.Usuario;
import com.bcsystems.contabilidad.domain.Nomina;
import com.bcsystems.contabilidad.domain.Venta;
import com.bcsystems.contabilidad.domain.en.EstadoComision;
import com.bcsystems.contabilidad.domain.en.TipoComision;
import com.bcsystems.contabilidad.dto.ComisionRegisterRequest;
import com.bcsystems.contabilidad.dto.ComisionResponse;
import com.bcsystems.contabilidad.dto.ComisionUpdateRequest;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.ComisionRepository;
import com.bcsystems.contabilidad.repository.UsuarioRepository;
import com.bcsystems.contabilidad.repository.VentaRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ComisionService {

    public static final BigDecimal PORCENTAJE_COMISION_VENTA = BigDecimal.valueOf(20);

    private final ComisionRepository comisionRepository;
    private final VentaRepository ventaRepository;
    private final UsuarioRepository usuarioRepository;

    public ComisionService(ComisionRepository comisionRepository,
                          VentaRepository ventaRepository,
                           UsuarioRepository usuarioRepository) {
        this.comisionRepository = comisionRepository;
        this.ventaRepository = ventaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Comision save(ComisionRegisterRequest request) {
        Optional<Venta> venta = request.ventaId() == null ? Optional.empty() : ventaRepository.findById(request.ventaId().intValue());
        if (request.ventaId() != null && venta.isEmpty()) throw new NotFoundException("Venta no encontrada");

        Optional<Usuario> usuario = usuarioRepository.findById(request.usuarioId().intValue());
        if (usuario.isEmpty()) {
            throw new NotFoundException("Usuario no encontrado");
        }

        BigDecimal montoComision = request.monto();
        if (montoComision == null && request.montoBase() != null && request.porcentajeComision() != null) {
            montoComision = request.montoBase()
                    .multiply(request.porcentajeComision())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        Comision comision = Comision.builder()
                .tipoComision(request.tipoComision())
                .concepto(request.concepto())
                .venta(venta.orElse(null))
                .usuario(usuario.get())
                .montoBase(request.montoBase() == null ? montoComision : request.montoBase())
                .porcentajeComision(request.porcentajeComision())
                .montoComision(montoComision)
                .estadoComision(EstadoComision.PENDIENTE)
                .fechaComision(LocalDate.now())
                .build();

        return comisionRepository.save(comision);
    }

    @Transactional
    public List<Comision> generateComisionesVenta(Long ventaId, Long empleadoVendedorId, 
                                                   Long empleadoCobradorId,
                                                   BigDecimal porcentajeVenta, 
                                                   BigDecimal porcentajeCobranza) {
        Optional<Venta> ventaOpt = ventaRepository.findById(ventaId.intValue());
        if (ventaOpt.isEmpty()) {
            throw new NotFoundException("Venta no encontrada");
        }

        Venta venta = ventaOpt.get();
        List<Comision> comisiones = new java.util.ArrayList<>();

        Optional<Usuario> vendedor = usuarioRepository.findById(empleadoVendedorId.intValue());
        if (vendedor.isPresent()) {
            BigDecimal montoComisionVenta = BigDecimal.valueOf(venta.getTotal())
                    .multiply(porcentajeVenta)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            Comision comisionVenta = Comision.builder()
                    .tipoComision(TipoComision.VENTA)
                    .venta(venta)
                    .usuario(vendedor.get())
                    .montoBase(BigDecimal.valueOf(venta.getTotal()))
                    .porcentajeComision(porcentajeVenta)
                    .montoComision(montoComisionVenta)
                    .estadoComision(EstadoComision.PENDIENTE)
                    .fechaComision(LocalDate.now())
                    .build();
            comisiones.add(comisionRepository.save(comisionVenta));
        }

        Optional<Usuario> cobrador = usuarioRepository.findById(empleadoCobradorId.intValue());
        if (cobrador.isPresent()) {
            BigDecimal montoComisionCobranza = BigDecimal.valueOf(venta.getTotal())
                    .multiply(porcentajeCobranza)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            Comision comisionCobranza = Comision.builder()
                    .tipoComision(TipoComision.COBRANZA)
                    .venta(venta)
                    .usuario(cobrador.get())
                    .montoBase(BigDecimal.valueOf(venta.getTotal()))
                    .porcentajeComision(porcentajeCobranza)
                    .montoComision(montoComisionCobranza)
                    .estadoComision(EstadoComision.PENDIENTE)
                    .fechaComision(LocalDate.now())
                    .build();
            comisiones.add(comisionRepository.save(comisionCobranza));
        }

        return comisiones;
    }

    @Transactional
    public List<Comision> findAll() {
        return comisionRepository.findAll();
    }

    public Page<ComisionResponse> findAllPageable(PageRequest pageRequest) {
        Page<Comision> comisiones = comisionRepository.findAll(pageRequest);
        return comisiones.map(this::toResponse);
    }

    @Transactional
    public Page<ComisionResponse> findFiltered(PageRequest request, String search, EstadoComision estado, Integer usuarioId, LocalDate desde, LocalDate hasta) {
        List<Comision> filtered = comisionRepository.findAll(request.getSort()).stream().filter(c ->
                (estado == null || c.getEstadoComision() == estado)
                        && (usuarioId == null || (c.getUsuario() != null && c.getUsuario().getIdUser().equals(usuarioId)))
                        && (desde == null || c.getFechaComision() == null || !c.getFechaComision().isBefore(desde))
                        && (hasta == null || c.getFechaComision() == null || !c.getFechaComision().isAfter(hasta))
                        && (search == null || search.isBlank() || (c.getConcepto() != null && c.getConcepto().toLowerCase().contains(search.toLowerCase()))
                        || (c.getUsuario() != null && c.getUsuario().getPersona() != null && c.getUsuario().getPersona().getNombre().toLowerCase().contains(search.toLowerCase())))).toList();
        int start = Math.min(request.getPageNumber() * request.getPageSize(), filtered.size());
        int end = Math.min(start + request.getPageSize(), filtered.size());
        return new PageImpl<>(filtered.subList(start, end).stream().map(this::toResponse).toList(), request, filtered.size());
    }

    public ComisionResponse toResponse(Comision comision) {
        return new ComisionResponse(
                comision.getIdComision(),
                comision.getTipoComision(),
                comision.getMontoBase(),
                comision.getPorcentajeComision(),
                comision.getMontoComision(),
                comision.getEstadoComision(),
                comision.getFechaComision(),
                comision.getFechaPago(),
                comision.getUsuario() != null ? comision.getUsuario().getIdUser().longValue() : null,
                comision.getVenta() != null ? comision.getVenta().getIdVenta().longValue() : null,
                comision.getConcepto(),
                comision.getUsuario() != null && comision.getUsuario().getPersona() != null
                        ? String.join(" ", java.util.stream.Stream.of(
                                comision.getUsuario().getPersona().getNombre(),
                                comision.getUsuario().getPersona().getPrimerApellido(),
                                comision.getUsuario().getPersona().getSegundoApellido())
                                .filter(java.util.Objects::nonNull).filter(s -> !s.isBlank()).toList()) : null
        );
    }

    public Optional<Comision> findById(Long id) {
        Optional<Comision> comision = comisionRepository.findById(id);
        if (comision.isPresent()) return comision;
        throw new NotFoundException("Comisión no encontrada");
    }

    public Optional<Comision> findById(Integer id) {
        return findById(id.longValue());
    }

    @Transactional
    public Comision update(Long id, ComisionUpdateRequest request) {
        Optional<Comision> comisionOpt = comisionRepository.findById(id);
        if (comisionOpt.isEmpty()) {
            throw new NotFoundException("Comisión no encontrada");
        }

        Comision comision = comisionOpt.get();

        if (request.tipoComision() != null) {
            comision.setTipoComision(request.tipoComision());
        }
        if (request.concepto() != null) comision.setConcepto(request.concepto().trim());
        if (request.estadoComision() != null) {
            comision.setEstadoComision(request.estadoComision());
            if (request.estadoComision() == EstadoComision.PAGADA) {
                comision.setFechaPago(LocalDate.now());
            }
        }
        if (request.montoBase() != null) {
            comision.setMontoBase(request.montoBase());
        }
        if (request.porcentajeComision() != null) {
            comision.setPorcentajeComision(request.porcentajeComision());
        }
        if (request.montoComision() != null) {
            comision.setMontoComision(request.montoComision());
        } else if (request.monto() != null) {
            comision.setMontoComision(request.monto());
        } else if (comision.getMontoBase() != null && comision.getPorcentajeComision() != null) {
            comision.setMontoComision(comision.getMontoBase().multiply(comision.getPorcentajeComision())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
        }
        if (request.fechaPago() != null) {
            comision.setFechaPago(request.fechaPago());
        }

        return comisionRepository.save(comision);
    }

    @Transactional
    public void delete(Long id) {
        Optional<Comision> comisionOpt = comisionRepository.findById(id);
        if (comisionOpt.isEmpty()) {
            throw new NotFoundException("Comisión no encontrada");
        }
        comisionRepository.delete(comisionOpt.get());
    }

    public List<Comision> findByVenta(Long ventaId) {
        return comisionRepository.findByVentaIdVenta(ventaId);
    }

    public List<Comision> findByUsuario(Long usuarioId) {
        return comisionRepository.findByUsuarioIdUser(usuarioId.intValue());
    }

    public List<Comision> findPendientes() {
        return comisionRepository.findByEstadoComision(EstadoComision.PENDIENTE);
    }

    public List<Comision> findPendientesByUsuario(Long usuarioId) {
        return comisionRepository.findByUsuarioIdUserAndEstadoComision(usuarioId.intValue(), EstadoComision.PENDIENTE);
    }

    @Transactional
    public List<Comision> pagarComisiones(List<Long> comisionIds) {
        List<Comision> comisiones = new java.util.ArrayList<>();
        for (Long id : comisionIds) {
            Optional<Comision> comisionOpt = comisionRepository.findById(id);
            if (comisionOpt.isPresent()) {
                Comision comision = comisionOpt.get();
                comision.setEstadoComision(EstadoComision.PAGADA);
                comision.setFechaPago(LocalDate.now());
                comisiones.add(comisionRepository.save(comision));
            }
        }
        return comisiones;
    }

    @Transactional
    public Comision generarComisionAutomaticaVenta(Venta venta, Usuario usuario) {
        BigDecimal montoBase = BigDecimal.valueOf(venta.getTotal());
        BigDecimal montoComision = montoBase
                .multiply(PORCENTAJE_COMISION_VENTA)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        Comision comision = Comision.builder()
                .tipoComision(TipoComision.VENTA)
                .venta(venta)
                .usuario(usuario)
                .montoBase(montoBase)
                .porcentajeComision(PORCENTAJE_COMISION_VENTA)
                .montoComision(montoComision)
                .estadoComision(EstadoComision.PENDIENTE)
                .fechaComision(LocalDate.now())
                .build();

        return comisionRepository.save(comision);
    }

    @Transactional
    public List<Comision> pagarComisionesPorNomina(List<Long> comisionIds, Nomina nomina) {
        List<Comision> comisiones = new ArrayList<>();
        if (comisionIds == null) return comisiones;
        for (Long id : comisionIds) {
            Optional<Comision> comisionOpt = comisionRepository.findById(id);
            if (comisionOpt.isPresent()) {
                Comision comision = comisionOpt.get();
                comision.setEstadoComision(EstadoComision.PAGADA);
                comision.setFechaPago(LocalDate.now());
                comision.setNomina(nomina);
                comisiones.add(comisionRepository.save(comision));
            }
        }
        return comisiones;
    }

    @Transactional
    public List<Comision> revertirComisionesDeNomina(Nomina nomina) {
        List<Comision> comisiones = comisionRepository.findByNominaIdNomina(nomina.getIdNomina());
        for (Comision comision : comisiones) {
            comision.setEstadoComision(EstadoComision.PENDIENTE);
            comision.setFechaPago(null);
            comision.setNomina(null);
            comisionRepository.save(comision);
        }
        return comisiones;
    }
}
