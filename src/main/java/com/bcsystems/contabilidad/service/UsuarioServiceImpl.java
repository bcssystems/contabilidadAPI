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
import java.text.Normalizer;
import java.util.Locale;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PersonaService personaService;
    private final HorarioService horarioService;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, PersonaService personaService, HorarioService horarioService) {
        this.usuarioRepository = usuarioRepository;
        this.personaService = personaService;
        this.horarioService = horarioService;
    }


    @Override
    public Usuario save(Usuario usuario) {
        if (usuarioRepository.findByUsername(usuario.getUsername()).isPresent()) return usuarioRepository.save(usuario);
        return usuarioRepository.save(usuario);
    }

    @Override
    public String generateUsername(String nombre, String primerApellido, String segundoApellido) {
        String[] nombres = normalize(nombre).split("\\s+");
        String surname = normalize(primerApellido);
        String initials = java.util.Arrays.stream(nombres).filter(s -> !s.isBlank()).map(s -> s.substring(0, 1)).collect(java.util.stream.Collectors.joining());
        String base = (initials + surname)
                .toLowerCase(Locale.ROOT);
        if (base.isBlank()) base = "usuario";
        String candidate = base;
        int suffix = 2;
        while (usuarioRepository.existsByUsername(candidate)) {
            candidate = base + suffix++;
        }
        return candidate;
    }

    private String normalize(String value) {
        if (value == null) return "";
        return Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^A-Za-z0-9 ]", "")
                .replaceAll("\\s+", " ");
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
                usr.getPersona() != null ? usr.getPersona().getNombre() : null,
                usr.getPersona() != null ? usr.getPersona().getPrimerApellido() : null,
                usr.getPersona() != null ? usr.getPersona().getSegundoApellido() : null,
                usr.getPersona() != null && usr.getPersona().getPersonType() != null ? usr.getPersona().getPersonType().toString() : null,
                usr.getActivo(),
                usr.getPersona() != null ? usr.getPersona().getFechaRegistro() : null,
                usr.getPosition() != null ? usr.getPosition().name() : null,
                usr.getSueldoBase(),
                 usr.getSueldoSemanal(),
                 usr.getSueldoHora()
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
                    usuario.getPersona() != null ? usuario.getPersona().getNombre() : null,
                    usuario.getPersona() != null ? usuario.getPersona().getPrimerApellido() : null,
                    usuario.getPersona() != null ? usuario.getPersona().getSegundoApellido() : null,
                    usuario.getPersona() != null && usuario.getPersona().getPersonType() != null ? usuario.getPersona().getPersonType().name() : null,
                    usuario.getActivo(),
                    usuario.getPersona() != null ? usuario.getPersona().getFechaRegistro() : null,
                    usuario.getPosition() != null ? usuario.getPosition().name() : null,
                    usuario.getSueldoBase(),
                     usuario.getSueldoSemanal(),
                     usuario.getSueldoHora()
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
        if(userUpdateRequest.position() != null) usr.setPosition(userUpdateRequest.position());
        if(userUpdateRequest.sueldoBase() != null) usr.setSueldoBase(userUpdateRequest.sueldoBase());
        if(userUpdateRequest.sueldoSemanal() != null) usr.setSueldoSemanal(userUpdateRequest.sueldoSemanal());
        if (userUpdateRequest.sueldoSemanal() != null && usr.getSueldoHora() == null) usr.setSueldoHora(java.math.BigDecimal.ZERO);

        Persona savedPersona = personaService.save(personaUpdate);
        usr.setPersona(savedPersona);
        Usuario saved = usuarioRepository.save(usr);
        horarioService.recalculateHourlyRate(saved.getIdUser());
        return saved;
    }
}
