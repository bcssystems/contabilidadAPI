package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Usuario;
import com.bcsystems.contabilidad.domain.auth.Token;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface TokenService {
    String extractUsername(String token);
    String generateToken(Usuario usuario);
    String generateRefreshToken(Usuario usuario);
    String buildToken(Usuario usuario, Long expiration);
    boolean isTokenValid(String token, Usuario usuario);
    boolean isTokenExpired(String token);
    Date extractExpiration(String token);
    SecretKey getSignInKey();
    Optional<Token> findByToken(String token);
    void saveToken(Token token);
    List<Token> findAllTokensByTrabajador(Usuario usuario);
    void deleteTokens(List<Token> tokens);
}
