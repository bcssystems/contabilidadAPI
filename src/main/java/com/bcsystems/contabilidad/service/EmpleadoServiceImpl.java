package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Empleado;
import com.bcsystems.contabilidad.domain.Persona;
import com.bcsystems.contabilidad.dto.EmpleadoRegisterRequest;
import com.bcsystems.contabilidad.dto.EmpleadoResponse;
import com.bcsystems.contabilidad.dto.EmpleadoUpdateRequest;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.EmpleadoRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmpleadoServiceImpl implements EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final PersonaService personaService;

    public EmpleadoServiceImpl(EmpleadoRepository empleadoRepository, PersonaService personaService) {
        this.empleadoRepository = empleadoRepository;
        this.personaService = personaService;
    }

    @Override
    @Transactional
    public Empleado save(EmpleadoRegisterRequest empleadoRegisterRequest) {
        Persona persona = Persona
                .builder()
                .nombre(empleadoRegisterRequest.nombre())
                .primerApellido(empleadoRegisterRequest.primerApellido())
                .segundoApellido(empleadoRegisterRequest.segundoApellido())
                .personType(empleadoRegisterRequest.personType())
                .build();
        Persona personasaved = personaService.save(persona);
        Empleado empleado = Empleado
                .builder()
                .activo(true)
                .position(empleadoRegisterRequest.position())
                .sueldoBase(empleadoRegisterRequest.sueldoBase())
                .persona(personasaved)
                .build();
        empleado = empleadoRepository.save(empleado);
        return empleado;
    }

    @Override
    public Optional<Empleado> findByNombre(String nombre) {
        Optional<Empleado> empleado = empleadoRepository.findEmpleadoByPersona_Nombre(nombre);
        if(empleado.isPresent()) return empleado;
        throw new NotFoundException("Usuario no encontrado");
    }

    @Override
    public List<Empleado> findAll() {
        return empleadoRepository.findAll();
    }

    @Override
    public Page<EmpleadoResponse> findAllPageable(PageRequest pageRequest) {
        Page<Empleado> empleados = empleadoRepository.findAll(pageRequest);
        return empleados.map(EmpleadoResponse::fromEntity);
    }

    @Override
    public Optional<Empleado> findById(Integer idUser) {
        Optional<Empleado> empleado = empleadoRepository.findByIdEmpleado(idUser);
        if(empleado.isPresent()) return empleado;
        throw new NotFoundException("Empleado no encontrado");
    }

    @Override
    @Transactional
    public Empleado updateEmpleado(Integer id, EmpleadoUpdateRequest empleadoUpdateRequest) {
        if(id == null) throw new NotFoundException("Usuario no encontrado");

        Optional<Empleado> user = empleadoRepository.findByIdEmpleado(id);
        if(!user.isPresent()) {
            throw new NotFoundException("Usuario no encontrado");
        }

        Empleado usr = user.get();
        Persona personaUpdate = new Persona();

        if(usr.getPersona() != null) {
            Integer idPersona = usr.getPersona().getIdPerson();
            Optional<Persona> persona = personaService.findById(idPersona);
            if(persona.isPresent()) {
                personaUpdate = persona.get();
            } else {
                personaUpdate = Persona.builder().build();
            }
        } else {
            personaUpdate = Persona.builder().build();
        }

        if(empleadoUpdateRequest.nombre() != null && !empleadoUpdateRequest.nombre().trim().isEmpty()) personaUpdate.setNombre(empleadoUpdateRequest.nombre().trim());
        if(empleadoUpdateRequest.primerApellido() != null && !empleadoUpdateRequest.primerApellido().trim().isEmpty()) personaUpdate.setPrimerApellido(empleadoUpdateRequest.primerApellido().trim());
        if(empleadoUpdateRequest.segundoApellido() != null && !empleadoUpdateRequest.segundoApellido().trim().isEmpty()) personaUpdate.setSegundoApellido(empleadoUpdateRequest.segundoApellido().trim());
        if(empleadoUpdateRequest.personType() != null) personaUpdate.setPersonType(empleadoUpdateRequest.personType());
        if(empleadoUpdateRequest.position() != null) usr.setPosition(empleadoUpdateRequest.position());
        if(empleadoUpdateRequest.sueldoBase() != null) usr.setSueldoBase(empleadoUpdateRequest.sueldoBase());
        if(empleadoUpdateRequest.activo() != null) usr.setActivo(empleadoUpdateRequest.activo());

        Persona savedPersona = personaService.save(personaUpdate);
        usr.setPersona(savedPersona);
        return empleadoRepository.save(usr);
    }
}
