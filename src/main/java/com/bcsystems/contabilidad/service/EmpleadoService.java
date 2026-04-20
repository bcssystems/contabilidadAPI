package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Empleado;
import com.bcsystems.contabilidad.dto.EmpleadoRegisterRequest;
import com.bcsystems.contabilidad.dto.EmpleadoResponse;
import com.bcsystems.contabilidad.dto.EmpleadoUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

public interface EmpleadoService {
    Empleado save(EmpleadoRegisterRequest empleadoRegisterRequest);
    Optional<Empleado> findByNombre(String nombre);
    List<Empleado> findAll();
    Page<EmpleadoResponse> findAllPageable(PageRequest pageRequest);
    Optional<Empleado> findById(Integer idUser);
    Empleado updateEmpleado(Integer id, EmpleadoUpdateRequest empleadoUpdateRequest);
}
