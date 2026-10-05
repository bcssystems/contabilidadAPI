package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.CorteCaja;
import com.bcsystems.contabilidad.domain.Deduccion;
import com.bcsystems.contabilidad.domain.Empleado;
import com.bcsystems.contabilidad.domain.Nomina;
import com.bcsystems.contabilidad.domain.en.EstadoDeduccion;
import com.bcsystems.contabilidad.domain.en.OrigenDeduccion;
import com.bcsystems.contabilidad.domain.en.TipoDeduccion;
import com.bcsystems.contabilidad.dto.DeduccionRegisterRequest;
import com.bcsystems.contabilidad.dto.DeduccionResponse;
import com.bcsystems.contabilidad.dto.DeduccionUpdateRequest;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.CorteCajaRepository;
import com.bcsystems.contabilidad.repository.DeduccionRepository;
import com.bcsystems.contabilidad.repository.EmpleadoRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DeduccionService {

    private final DeduccionRepository deduccionRepository;
    private final EmpleadoRepository empleadoRepository;
    private final CorteCajaRepository corteCajaRepository;

    public DeduccionService(DeduccionRepository deduccionRepository,
                            EmpleadoRepository empleadoRepository,
                            CorteCajaRepository corteCajaRepository) {
        this.deduccionRepository = deduccionRepository;
        this.empleadoRepository = empleadoRepository;
        this.corteCajaRepository = corteCajaRepository;
    }

    @Transactional
    public Deduccion save(DeduccionRegisterRequest request) {
        Empleado empleado = empleadoRepository.findById(request.empleadoId())
                .orElseThrow(() -> new NotFoundException("Empleado no encontrado"));

        Deduccion deduccion = Deduccion.builder()
                .empleado(empleado)
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

    public Page<DeduccionResponse> findAllPageable(PageRequest pageRequest) {
        return deduccionRepository.findAll(pageRequest).map(DeduccionResponse::fromEntity);
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

    public List<DeduccionResponse> findByEmpleado(Integer empleadoId) {
        return deduccionRepository.findByEmpleadoIdEmpleado(empleadoId)
                .stream().map(DeduccionResponse::fromEntity).toList();
    }

    public List<Deduccion> findPendientesByEmpleado(Integer empleadoId) {
        return deduccionRepository.findByEmpleadoIdEmpleadoAndEstado(empleadoId, EstadoDeduccion.PENDIENTE);
    }

    public List<DeduccionResponse> findPendientesByEmpleadoResponse(Integer empleadoId) {
        return deduccionRepository.findByEmpleadoIdEmpleadoAndEstado(empleadoId, EstadoDeduccion.PENDIENTE)
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
            deduccionRepository.save(deduccion);
        }
        return deducciones;
    }
}
