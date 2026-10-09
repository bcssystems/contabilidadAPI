package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Deduccion;
import com.bcsystems.contabilidad.domain.PrestamoEmpleado;
import com.bcsystems.contabilidad.domain.PrestamoParcialidad;
import com.bcsystems.contabilidad.domain.Usuario;
import com.bcsystems.contabilidad.domain.en.*;
import com.bcsystems.contabilidad.dto.*;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PrestamoEmpleadoService {
    private final PrestamoEmpleadoRepository prestamoRepository;
    private final PrestamoParcialidadRepository parcialidadRepository;
    private final UsuarioRepository usuarioRepository;
    private final DeduccionRepository deduccionRepository;

    public PrestamoEmpleadoService(PrestamoEmpleadoRepository prestamoRepository,
                                   PrestamoParcialidadRepository parcialidadRepository,
                                   UsuarioRepository usuarioRepository, DeduccionRepository deduccionRepository) {
        this.prestamoRepository = prestamoRepository;
        this.parcialidadRepository = parcialidadRepository;
        this.usuarioRepository = usuarioRepository;
        this.deduccionRepository = deduccionRepository;
    }

    @Transactional
    public PrestamoEmpleadoResponse crear(PrestamoEmpleadoRequest request) {
        LocalDate fechaPrimerPago = request.fechaPrimerPago() == null ? LocalDate.now().plusWeeks(1) : request.fechaPrimerPago();
        validar(request.usuarioId(), request.montoPrincipal(), request.numeroParcialidades(), fechaPrimerPago);
        Usuario usuario = usuarioRepository.findById(request.usuarioId()).orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        PrestamoEmpleado prestamo = PrestamoEmpleado.builder().usuario(usuario).montoPrincipal(request.montoPrincipal().setScale(2, RoundingMode.HALF_UP))
                .tasaInteresPorcentaje(request.tasaInteresPorcentaje() == null ? BigDecimal.ZERO : request.tasaInteresPorcentaje())
                .numeroParcialidades(request.numeroParcialidades()).fechaCreacion(LocalDateTime.now()).fechaPrimerPago(fechaPrimerPago)
                .estado(EstadoPrestamo.ACTIVO).build();
        generarCalendario(prestamo);
        return PrestamoEmpleadoResponse.fromEntity(prestamoRepository.save(prestamo));
    }

    private void validar(Integer usuarioId, BigDecimal monto, Integer numero, LocalDate fechaPrimerPago) {
        if (usuarioId == null || monto == null || monto.signum() <= 0 || numero == null || numero <= 0 || numero > 520)
            throw new IllegalArgumentException("Usuario, monto y número de parcialidades son requeridos y válidos");
        if (fechaPrimerPago.isBefore(LocalDate.of(2000, 1, 1))) throw new IllegalArgumentException("Primera fecha de pago inválida");
    }

    private void generarCalendario(PrestamoEmpleado p) {
        BigDecimal saldo = p.getMontoPrincipal();
        BigDecimal capitalBase = p.getMontoPrincipal().divide(BigDecimal.valueOf(p.getNumeroParcialidades()), 2, RoundingMode.HALF_UP);
        BigDecimal interesTotal = p.getMontoPrincipal()
                .multiply(p.getTasaInteresPorcentaje())
                .multiply(BigDecimal.valueOf(p.getNumeroParcialidades()))
                .divide(BigDecimal.valueOf(100 * 4L), 2, RoundingMode.HALF_UP);
        BigDecimal pagoBase = p.getMontoPrincipal().add(interesTotal)
                .divide(BigDecimal.valueOf(p.getNumeroParcialidades()), 2, RoundingMode.HALF_UP);
        for (int i = 1; i <= p.getNumeroParcialidades(); i++) {
            BigDecimal capital = i == p.getNumeroParcialidades() ? saldo : capitalBase;
            BigDecimal monto = i == p.getNumeroParcialidades()
                    ? p.getMontoPrincipal().add(interesTotal).subtract(pagoBase.multiply(BigDecimal.valueOf(i - 1L)))
                    : pagoBase;
            BigDecimal interes = monto.subtract(capital).setScale(2, RoundingMode.HALF_UP);
            saldo = saldo.subtract(capital).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
            p.getParcialidades().add(PrestamoParcialidad.builder().prestamo(p).numero(i).capital(capital).interes(interes)
                    .monto(monto).saldo(saldo).fechaVencimiento(p.getFechaPrimerPago().plusWeeks(i - 1L)).estado(EstadoParcialidad.PENDIENTE).build());
        }
    }

    @Transactional
    public Page<PrestamoEmpleadoResponse> listar(PageRequest pageRequest) {
        return prestamoRepository.findAll(pageRequest).map(PrestamoEmpleadoResponse::fromEntity);
    }

    @Transactional
    public Page<PrestamoEmpleadoResponse> listarFiltrado(PageRequest request, String search, EstadoPrestamo estado) {
        List<PrestamoEmpleado> filtered = prestamoRepository.findAll(request.getSort()).stream().filter(p ->
                (estado == null || p.getEstado() == estado)
                        && (search == null || search.isBlank() || (p.getUsuario().getPersona() != null
                        && (p.getUsuario().getPersona().getNombre() + " " + p.getUsuario().getPersona().getPrimerApellido()).toLowerCase().contains(search.toLowerCase())))).toList();
        int start = Math.min(request.getPageNumber() * request.getPageSize(), filtered.size());
        int end = Math.min(start + request.getPageSize(), filtered.size());
        return new PageImpl<>(filtered.subList(start, end).stream().map(PrestamoEmpleadoResponse::fromEntity).toList(), request, filtered.size());
    }

    @Transactional
    public PrestamoEmpleadoResponse detalle(Long id) { return PrestamoEmpleadoResponse.fromEntity(obtener(id)); }

    @Transactional
    public PrestamoEmpleadoResponse actualizar(Long id, PrestamoEmpleadoUpdateRequest request) {
        PrestamoEmpleado p = obtener(id);
        if (p.getEstado() != EstadoPrestamo.ACTIVO) throw new IllegalArgumentException("Solo se puede actualizar un préstamo activo");
        if (p.getParcialidades().stream().anyMatch(x -> x.getEstado() == EstadoParcialidad.PAGADA)) {
            throw new IllegalArgumentException("No se puede editar un préstamo con parcialidades pagadas");
        }
        if (request.montoPrincipal() != null) p.setMontoPrincipal(request.montoPrincipal());
        if (request.numeroParcialidades() != null) p.setNumeroParcialidades(request.numeroParcialidades());
        if (request.tasaInteresPorcentaje() != null) p.setTasaInteresPorcentaje(request.tasaInteresPorcentaje());
        if (request.fechaPrimerPago() != null) p.setFechaPrimerPago(request.fechaPrimerPago());
        if (p.getFechaPrimerPago() == null) p.setFechaPrimerPago(LocalDate.now().plusWeeks(1));
        p.getParcialidades().forEach(parcialidad -> deduccionRepository.deleteAll(
                deduccionRepository.findByPrestamoParcialidadIdParcialidad(parcialidad.getIdParcialidad())));
        p.getParcialidades().clear();
        generarCalendario(p);
        return PrestamoEmpleadoResponse.fromEntity(prestamoRepository.save(p));
    }

    @Transactional
    public PrestamoEmpleadoResponse cancelar(Long id) {
        PrestamoEmpleado p = obtener(id);
        p.setEstado(EstadoPrestamo.CANCELADO);
        p.getParcialidades().stream().filter(x -> x.getEstado() != EstadoParcialidad.PAGADA).forEach(x -> x.setEstado(EstadoParcialidad.CANCELADA));
        return PrestamoEmpleadoResponse.fromEntity(prestamoRepository.save(p));
    }

    @Transactional
    public List<PrestamoParcialidadResponse> parcialidades(Long id) {
        obtener(id);
        return parcialidadRepository.findByPrestamoIdPrestamoOrderByNumero(id).stream().map(PrestamoParcialidadResponse::fromEntity).toList();
    }

    private PrestamoEmpleado obtener(Long id) { return prestamoRepository.findById(id).orElseThrow(() -> new NotFoundException("Préstamo no encontrado")); }

    @Transactional
    public void generarDeduccionesParaNomina(Integer usuarioId, LocalDate fechaPago) {
        generarParaUsuario(usuarioId, fechaPago);
    }

    @Transactional
    public void generarDeduccionesParaTodos(LocalDate fechaPago) {
        prestamoRepository.findAll().stream().map(p -> p.getUsuario().getIdUser()).distinct()
                .forEach(usuarioId -> generarParaUsuario(usuarioId, fechaPago));
    }

    private void generarParaUsuario(Integer usuarioId, LocalDate fechaPago) {
        for (PrestamoEmpleado p : prestamoRepository.findAll().stream().filter(x -> x.getUsuario().getIdUser().equals(usuarioId) && x.getEstado() == EstadoPrestamo.ACTIVO).toList()) {
            for (PrestamoParcialidad parcialidad : p.getParcialidades()) {
                if (parcialidad.getEstado() == EstadoParcialidad.PENDIENTE && parcialidad.getFechaVencimiento().isBefore(fechaPago)) parcialidad.setEstado(EstadoParcialidad.VENCIDA);
                if ((parcialidad.getEstado() == EstadoParcialidad.PENDIENTE || parcialidad.getEstado() == EstadoParcialidad.VENCIDA)
                        && !parcialidad.getFechaVencimiento().isAfter(fechaPago) && !deduccionRepository.existsByPrestamoParcialidadIdParcialidad(parcialidad.getIdParcialidad())) {
                    deduccionRepository.save(Deduccion.builder().usuario(p.getUsuario()).concepto("Parcialidad de préstamo " + p.getIdPrestamo() + " #" + parcialidad.getNumero())
                            .monto(parcialidad.getMonto()).fecha(parcialidad.getFechaVencimiento()).estado(EstadoDeduccion.PENDIENTE)
                            .tipo(TipoDeduccion.PRESTAMO).origen(OrigenDeduccion.PRESTAMO).prestamoParcialidad(parcialidad).build());
                }
            }
            prestamoRepository.save(p);
        }
    }
}
