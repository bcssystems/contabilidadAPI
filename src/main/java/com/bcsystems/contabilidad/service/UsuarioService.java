package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Usuario;
import com.bcsystems.contabilidad.dto.UserDetailedResponse;
import com.bcsystems.contabilidad.dto.UserUpdateRequest;

import java.util.List;
import java.util.Optional;

public interface UsuarioService {
    Usuario save(Usuario usuario);
    Optional<Usuario> findByUsername(String username);
    UserDetailedResponse findByUsernameDetailed(String username);
    List<UserDetailedResponse> findAll();
    Optional<Usuario> findById(Integer idUser);
    Usuario updateUser(Integer id, UserUpdateRequest userUpdateRequest);
}
