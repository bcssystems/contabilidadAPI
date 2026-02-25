package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.AsientoContable;
import com.bcsystems.contabilidad.domain.Empleado;
import com.bcsystems.contabilidad.domain.Nomina;
import com.bcsystems.contabilidad.dto.NominaRegisterRequest;
import com.bcsystems.contabilidad.dto.NominaUpdateRequest;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.AsientoContableRepository;
import com.bcsystems.contabilidad.repository.EmpleadoRepository;
import com.bcsystems.contabilidad.repository.NominaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NominaService {

    private final NominaRepository nominaRepository;
    private final EmpleadoRepository empleadoRepository;
    private final AsientoContableRepository asientoContableRepository;

    public NominaService(NominaRepository nominaRepository,
                        EmpleadoRepository empleadoRepository,
                        AsientoContableRepository asientoContableRepository) {
        this.nominaRepository = nominaRepository;
        this.empleadoRepository = empleadoRepository;
        this.asientoContableRepository = asientoContableRepository;
    }

    @Transactional
    public Nomina save(NominaRegisterRequest request) {
        Nomina nomina = Nomina.builder()
                .fecha_nomina(request.fecha_nomina())
                .sueldo(request.sueldo())
                .build();

        if (request.empleadoId() != null) {
            Optional<Empleado> empleado = empleadoRepository.findById(request.empleadoId());
            if (empleado.isPresent()) {
                nomina.setEmpleado(empleado.get());
            }
        }

        if (request.asientoId() != null) {
            Optional<AsientoContable> asiento = asientoContableRepository.findById(request.asientoId());
            if (asiento.isPresent()) {
                nomina.setAsientoContable(asiento.get());
            }
        }

        return nominaRepository.save(nomina);
    }

    public List<Nomina> findAll() {
        return nominaRepository.findAll();
    }

    public Optional<Nomina> findById(Long id) {
        Optional<Nomina> nomina = nominaRepository.findById(id);
        if (nomina.isPresent()) return nomina;
        throw new NotFoundException("Nómina no encontrada");
    }

    @Transactional
    public Nomina update(Long id, NominaUpdateRequest request) {
        Optional<Nomina> nominaOpt = nominaRepository.findById(id);
        if (!nominaOpt.isPresent()) {
            throw new NotFoundException("Nómina no encontrada");
        }

        Nomina nomina = nominaOpt.get();

        if (request.fecha_nomina() != null) {
            nomina.setFecha_nomina(request.fecha_nomina());
        }
        if (request.sueldo() != null) {
            nomina.setSueldo(request.sueldo());
        }
        if (request.empleadoId() != null) {
            Optional<Empleado> empleado = empleadoRepository.findById(request.empleadoId());
            if (empleado.isPresent()) {
                nomina.setEmpleado(empleado.get());
            }
        }
        if (request.asientoId() != null) {
            Optional<AsientoContable> asiento = asientoContableRepository.findById(request.asientoId());
            if (asiento.isPresent()) {
                nomina.setAsientoContable(asiento.get());
            }
        }

        return nominaRepository.save(nomina);
    }

    @Transactional
    public void delete(Long id) {
        Optional<Nomina> nominaOpt = nominaRepository.findById(id);
        if (!nominaOpt.isPresent()) {
            throw new NotFoundException("Nómina no encontrada");
        }
        nominaRepository.delete(nominaOpt.get());
    }
}
