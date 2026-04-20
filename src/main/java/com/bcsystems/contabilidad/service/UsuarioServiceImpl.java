package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Persona;
import com.bcsystems.contabilidad.domain.Usuario;
import com.bcsystems.contabilidad.dto.UserDetailedResponse;
import com.bcsystems.contabilidad.dto.UserUpdateRequest;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PersonaService personaService;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, PersonaService personaService) {
        this.usuarioRepository = usuarioRepository;
        this.personaService = personaService;
    }


    @Override
    public Usuario save(Usuario usuario) {
        if (usuarioRepository.findByUsername(usuario.getUsername()).isPresent()) return usuarioRepository.save(usuario);
        return usuarioRepository.save(usuario);
    }

    @Override
    public Optional<Usuario> findByUsername(String username) {
        Optional<Usuario> user = usuarioRepository.findByUsername(username);
        if (user.isPresent()) return user;
        else throw new NotFoundException("User not found");
    }

    @Override
    public UserDetailedResponse findByUsernameDetailed(String username) {
        Optional<Usuario> user = usuarioRepository.findByUsername(username);
        Usuario usr = user.get();
        if (user.isPresent()) return new UserDetailedResponse(
                usr.getIdUser(),
                usr.getUsername(),
                usr.getRole().toString(),
                usr.getPersona().getNombre(),
                usr.getPersona().getPrimerApellido(),
                usr.getPersona().getSegundoApellido(),
                usr.getPersona().getPersonType().toString(),
                usr.getActivo(),
                usr.getPersona().getFechaRegistro()
        );
        else throw new NotFoundException("User not found");
    }

    @Override
    public List<UserDetailedResponse> findAll() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        List<UserDetailedResponse> users = new ArrayList<>();
        for (Usuario usuario : usuarios) {
            users.add(new UserDetailedResponse(
                    usuario.getIdUser(),
                    usuario.getUsername(),
                    usuario.getRole().name(),
                    usuario.getPersona().getNombre(),
                    usuario.getPersona().getPrimerApellido(),
                    usuario.getPersona().getSegundoApellido(),
                    usuario.getPersona().getPersonType().name(),
                    usuario.getActivo(),
                    usuario.getPersona().getFechaRegistro()
            ));
        }
        return users;
    }

    @Override
    public Optional<Usuario> findById(Integer idUser) {
        Optional<Usuario> user = usuarioRepository.findById(idUser);
        if(user.isPresent()) return user;
        else throw new NotFoundException("User not found");
    }

    @Override
    public Usuario updateUser(Integer id, UserUpdateRequest userUpdateRequest) {
        if(id == null) throw new NotFoundException("Usuario no encontrado");

        Optional<Usuario> user = usuarioRepository.findById(id);
        if(!user.isPresent()) {
            throw new NotFoundException("Usuario no encontrado");
        }

        Usuario usr = user.get();
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

        if(userUpdateRequest.nombre() != null && !userUpdateRequest.nombre().trim().isEmpty()) personaUpdate.setNombre(userUpdateRequest.nombre().trim());
        if(userUpdateRequest.primerApellido() != null && !userUpdateRequest.primerApellido().trim().isEmpty()) personaUpdate.setPrimerApellido(userUpdateRequest.primerApellido().trim());
        if(userUpdateRequest.segundoApellido() != null && !userUpdateRequest.segundoApellido().trim().isEmpty()) personaUpdate.setSegundoApellido(userUpdateRequest.segundoApellido().trim());
        if(userUpdateRequest.role() != null) usr.setRole(userUpdateRequest.role());
        if(userUpdateRequest.personType() != null) personaUpdate.setPersonType(userUpdateRequest.personType());
        if(userUpdateRequest.activo() != null) usr.setActivo(userUpdateRequest.activo());

        Persona savedPersona = personaService.save(personaUpdate);
        usr.setPersona(savedPersona);
        return usuarioRepository.save(usr);
    }
}
