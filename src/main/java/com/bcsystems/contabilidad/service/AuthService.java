package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Usuario;
import com.bcsystems.contabilidad.dto.TokenResponse;
import com.bcsystems.contabilidad.dto.UserAuthRequest;
import com.bcsystems.contabilidad.dto.UserRegisterRequest;

public interface AuthService {
    TokenResponse register(UserRegisterRequest userRegisterRequest);
    TokenResponse login(UserAuthRequest userAuthRequest);
    void saveUserToken(Usuario usuario, String token);
    void removeAllUserTokens(Usuario usuario);
    TokenResponse refreshToken(String authentication);
}
