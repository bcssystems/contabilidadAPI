package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Usuario;
import com.bcsystems.contabilidad.dto.UserDetailedResponse;
import com.bcsystems.contabilidad.dto.UserUpdateRequest;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

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
        return List.of();
    }

    @Override
    public Optional<Usuario> findById(Integer idUser) {
        return Optional.empty();
    }

    @Override
    public Usuario updateUser(Integer id, UserUpdateRequest trabajadorUpdateRequest) {
        return null;
    }
}
