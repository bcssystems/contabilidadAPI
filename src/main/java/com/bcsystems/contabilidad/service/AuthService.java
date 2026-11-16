package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.dto.TokenResponse;
import com.bcsystems.contabilidad.dto.UserAuthRequest;
import com.bcsystems.contabilidad.dto.UserRegisterRequest;

public interface AuthService {
    TokenResponse register(UserRegisterRequest userRegisterRequest);
    TokenResponse login(UserAuthRequest userAuthRequest);
    TokenResponse refreshToken(String authentication);
    void logout(String accessToken, String refreshToken);
}
