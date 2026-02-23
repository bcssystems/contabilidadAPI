package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Persona;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.PersonaRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PersonaServiceImpl implements PersonaService {

    private final PersonaRepository personaRepository;

    public PersonaServiceImpl(PersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
    }


    @Override
    public Persona save(Persona persona) {
        return personaRepository.save(persona);
    }

    @Override
    public Optional<Persona> findById(Integer idPersona) {
        Optional<Persona> person = personaRepository.findById(idPersona);
        if (person.isPresent()) return person;
        else throw new NotFoundException("Person not found");
    }

    @Override
    public Optional<Persona> findByNombre(String nombre) {
        Optional<Persona> person = personaRepository.findByNombre(nombre);
        if (person.isPresent()) return person;
        else throw new NotFoundException("Person not found");
    }
}
