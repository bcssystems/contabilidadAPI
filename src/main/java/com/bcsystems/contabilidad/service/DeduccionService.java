package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.CorteCaja;
import com.bcsystems.contabilidad.domain.Deduccion;
import com.bcsystems.contabilidad.domain.Usuario;
import com.bcsystems.contabilidad.domain.Nomina;
import com.bcsystems.contabilidad.domain.PrestamoParcialidad;
import com.bcsystems.contabilidad.domain.PrestamoEmpleado;
import com.bcsystems.contabilidad.domain.en.EstadoParcialidad;
import com.bcsystems.contabilidad.domain.en.EstadoPrestamo;
import com.bcsystems.contabilidad.domain.en.EstadoDeduccion;
import com.bcsystems.contabilidad.domain.en.OrigenDeduccion;
import com.bcsystems.contabilidad.domain.en.TipoDeduccion;
import com.bcsystems.contabilidad.dto.DeduccionRegisterRequest;
import com.bcsystems.contabilidad.dto.DeduccionResponse;
import com.bcsystems.contabilidad.dto.DeduccionUpdateRequest;
import com.bcsystems.contabilidad.dto.DeduccionBatchRequest;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.CorteCajaRepository;
import com.bcsystems.contabilidad.repository.DeduccionRepository;
import com.bcsystems.contabilidad.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DeduccionService {

    private final DeduccionRepository deduccionRepository;
    private final UsuarioRepository usuarioRepository;
    private final CorteCajaRepository corteCajaRepository;

    public DeduccionService(DeduccionRepository deduccionRepository,
                            UsuarioRepository usuarioRepository,
                            CorteCajaRepository corteCajaRepository) {
        this.deduccionRepository = deduccionRepository;
        this.usuarioRepository = usuarioRepository;
        this.corteCajaRepository = corteCajaRepository;
    }

    @Transactional
    public Deduccion save(DeduccionRegisterRequest request) {
        Usuario usuario = usuarioRepository.findById(request.usuarioId().intValue())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        Deduccion deduccion = Deduccion.builder()
                .usuario(usuario)
                .concepto(request.concepto())
                .monto(request.monto())
                .fecha(request.fecha() != null ? request.fecha() : LocalDate.now())
                .estado(EstadoDeduccion.PENDIENTE)
                .tipo(TipoDeduccion.MANUAL)
                .origen(OrigenDeduccion.MANUAL)
                .build();

        if (request.corteCajaId() != null) {
            Optional<CorteCaja> corte = corteCajaRepository.findById(request.corteCajaId().intValue());
            corte.ifPresent(deduccion::setCorteCaja);
        }

        return deduccionRepository.save(deduccion);
    }

    @Transactional
    public List<Deduccion> saveBatch(DeduccionBatchRequest request) {
        if (request == null || request.deducciones() == null || request.deducciones().isEmpty()) {
            throw new IllegalArgumentException("Agrega al menos una deducción");
        }
        return request.deducciones().stream().map(this::save).toList();
    }

    public Page<DeduccionResponse> findAllPageable(PageRequest pageRequest) {
        return deduccionRepository.findAll(pageRequest).map(DeduccionResponse::fromEntity);
    }

    public Page<DeduccionResponse> findFiltered(PageRequest request, String search, EstadoDeduccion estado, OrigenDeduccion origen, Integer usuarioId, LocalDate desde, LocalDate hasta) {
        List<Deduccion> filtered = deduccionRepository.findAll(request.getSort()).stream().filter(d ->
                (estado == null || d.getEstado() == estado) && (origen == null || d.getOrigen() == origen)
                        && (usuarioId == null || (d.getUsuario() != null && d.getUsuario().getIdUser().equals(usuarioId)))
                        && (desde == null || d.getFecha() == null || !d.getFecha().isBefore(desde))
                        && (hasta == null || d.getFecha() == null || !d.getFecha().isAfter(hasta))
                        && (search == null || search.isBlank() || (d.getConcepto() != null && d.getConcepto().toLowerCase().contains(search.toLowerCase())))).toList();
        int start = Math.min(request.getPageNumber() * request.getPageSize(), filtered.size());
        int end = Math.min(start + request.getPageSize(), filtered.size());
        return new PageImpl<>(filtered.subList(start, end).stream().map(DeduccionResponse::fromEntity).toList(), request, filtered.size());
    }

    public DeduccionResponse findById(Long id) {
        return deduccionRepository.findById(id)
                .map(DeduccionResponse::fromEntity)
                .orElseThrow(() -> new NotFoundException("Deducción no encontrada"));
    }

    @Transactional
    public DeduccionResponse update(Long id, DeduccionUpdateRequest request) {
        Deduccion deduccion = deduccionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Deducción no encontrada"));

        if (request.concepto() != null && !request.concepto().trim().isEmpty()) {
            deduccion.setConcepto(request.concepto().trim());
        }
        if (request.monto() != null) {
            deduccion.setMonto(request.monto());
        }
        if (request.fecha() != null) {
            deduccion.setFecha(request.fecha());
        }
        if (request.corteCajaId() != null) {
            Optional<CorteCaja> corte = corteCajaRepository.findById(request.corteCajaId().intValue());
            corte.ifPresent(deduccion::setCorteCaja);
        }
        if (request.estado() != null && deduccion.getNomina() == null) {
            deduccion.setEstado(request.estado());
        }

        return DeduccionResponse.fromEntity(deduccionRepository.save(deduccion));
    }

    @Transactional
    public void delete(Long id) {
        Deduccion deduccion = deduccionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Deducción no encontrada"));
        if (deduccion.getNomina() != null) {
            throw new NotFoundException("La deducción ya está ligada a una nómina y no puede eliminarse");
        }
        deduccionRepository.delete(deduccion);
    }

    public List<DeduccionResponse> findByUsuario(Integer usuarioId) {
        return deduccionRepository.findByUsuarioIdUser(usuarioId)
                .stream().map(DeduccionResponse::fromEntity).toList();
    }

    public List<Deduccion> findPendientesByUsuario(Integer usuarioId) {
        return deduccionRepository.findByUsuarioIdUserAndEstadoIn(usuarioId,
                List.of(EstadoDeduccion.PENDIENTE, EstadoDeduccion.APROBADA));
    }

    public List<DeduccionResponse> findPendientesByUsuarioResponse(Integer usuarioId) {
        return deduccionRepository.findByUsuarioIdUserAndEstadoIn(usuarioId,
                        List.of(EstadoDeduccion.PENDIENTE, EstadoDeduccion.APROBADA))
                .stream().map(DeduccionResponse::fromEntity).toList();
    }

    @Transactional
    public DeduccionResponse reviewAttendance(Long id, boolean approve) {
        Deduccion deduction = deduccionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Deducción no encontrada"));
        if (deduction.getOrigen() != OrigenDeduccion.ASISTENCIA || deduction.getNomina() != null
                || deduction.getEstado() != EstadoDeduccion.PENDIENTE) {
            throw new IllegalArgumentException("La deducción no es una incidencia de asistencia pendiente");
        }
        deduction.setEstado(approve ? EstadoDeduccion.APROBADA : EstadoDeduccion.RECHAZADA);
        return DeduccionResponse.fromEntity(deduccionRepository.save(deduction));
    }

    @Transactional
    public List<Deduccion> aplicarDeduccionesPorNomina(List<Long> deduccionIds, Nomina nomina) {
        List<Deduccion> deducciones = new ArrayList<>();
        if (deduccionIds == null) return deducciones;
        for (Long id : deduccionIds) {
            Optional<Deduccion> deduccionOpt = deduccionRepository.findById(id);
            if (deduccionOpt.isPresent()) {
                Deduccion deduccion = deduccionOpt.get();
                deduccion.setEstado(EstadoDeduccion.APLICADA);
                deduccion.setNomina(nomina);
                PrestamoParcialidad parcialidad = deduccion.getPrestamoParcialidad();
                if (parcialidad != null) {
                    parcialidad.setEstado(EstadoParcialidad.PAGADA);
                    PrestamoEmpleado prestamo = parcialidad.getPrestamo();
                    if (prestamo.getParcialidades().stream().allMatch(p -> p.getEstado() == EstadoParcialidad.PAGADA)) {
                        prestamo.setEstado(EstadoPrestamo.LIQUIDADO);
                    }
                }
                deducciones.add(deduccionRepository.save(deduccion));
            }
        }
        return deducciones;
    }

    @Transactional
    public List<Deduccion> revertirDeduccionesDeNomina(Nomina nomina) {
        List<Deduccion> deducciones = deduccionRepository.findByNominaIdNomina(nomina.getIdNomina());
        for (Deduccion deduccion : deducciones) {
            deduccion.setEstado(EstadoDeduccion.PENDIENTE);
            deduccion.setNomina(null);
            PrestamoParcialidad parcialidad = deduccion.getPrestamoParcialidad();
            if (parcialidad != null) {
                parcialidad.setEstado(parcialidad.getFechaVencimiento().isBefore(LocalDate.now())
                        ? EstadoParcialidad.VENCIDA : EstadoParcialidad.PENDIENTE);
                parcialidad.getPrestamo().setEstado(EstadoPrestamo.ACTIVO);
            }
            deduccionRepository.save(deduccion);
        }
        return deducciones;
    }
}
