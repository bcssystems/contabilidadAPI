package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Usuario;
import com.bcsystems.contabilidad.domain.auth.Token;

import java.util.List;
import java.util.Optional;

public interface TokenService {
    String extractUsername(String token);
    Token generateTokenPair(Usuario usuario, String sessionFamily);
    boolean isTokenValid(String token, Usuario usuario);
    String tokenType(String token);
    boolean belongsToSiga(String token);
    Optional<Token> findByTokenHash(String tokenHash);
    Optional<Token> findByRefreshTokenHash(String tokenHash);
    void saveToken(Token token);
    List<Token> findAllValidTokensByUsuario(Usuario usuario);
    int consumeRefreshToken(Integer id);
    int revokeAllBySessionFamily(String sessionFamily);
    String hashToken(String token);
}
