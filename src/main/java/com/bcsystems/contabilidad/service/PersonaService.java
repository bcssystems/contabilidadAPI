package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Persona;

import java.util.Optional;

public interface PersonaService {
    Persona save(Persona persona);
    Optional<Persona> findById(Integer idPersona);
    Optional<Persona> findByNombre(String nombre);
}
