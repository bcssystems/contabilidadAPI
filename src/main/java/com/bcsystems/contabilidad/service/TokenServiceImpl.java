package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Usuario;
import com.bcsystems.contabilidad.domain.auth.Token;
import com.bcsystems.contabilidad.domain.en.TokenType;
import com.bcsystems.contabilidad.repository.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class TokenServiceImpl implements TokenService {
    private final TokenRepository tokenRepository;

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;
    @Value("${application.security.jwt.expiration:900000}")
    private long jwtExpiration;
    @Value("${application.security.jwt.refresh-token.expiration:604800000}")
    private long refreshExpiration;

    public TokenServiceImpl(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Override
    public Token generateTokenPair(Usuario usuario, String sessionFamily) {
        String family = sessionFamily == null ? UUID.randomUUID().toString() : sessionFamily;
        String access = buildToken(usuario, jwtExpiration, "access", family);
        String refresh = buildToken(usuario, refreshExpiration, "refresh", family);
        return Token.builder()
                .token(access)
                .refreshToken(refresh)
                .tokenHash(hashToken(access))
                .refreshTokenHash(hashToken(refresh))
                .sessionFamily(family)
                .type(TokenType.BEARER)
                .isExpired(false)
                .isRevoked(false)
                .usuario(usuario)
                .build();
    }

    private String buildToken(Usuario usuario, long expiration, String type, String family) {
        return Jwts.builder()
                .claims(Map.of("name", usuario.getPersona() == null ? usuario.getUsername() : usuario.getPersona().getNombre()))
                .claim("issuer", "SIGA")
                .claim("audience", "SIGA_API")
                .claim("token_type", type)
                .claim("session_family", family)
                .subject(usuario.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    @Override
    public String extractUsername(String token) {
        return parse(token).getSubject();
    }

    @Override
    public boolean isTokenValid(String token, Usuario usuario) {
        try {
            Claims claims = parse(token);
            return usuario.getUsername().equals(claims.getSubject())
                    && "SIGA".equals(claims.get("issuer", String.class))
                    && "SIGA_API".equals(claims.get("audience", String.class))
                    && !claims.getExpiration().before(new Date());
        } catch (RuntimeException ex) {
            return false;
        }
    }

    @Override
    public String tokenType(String token) { return parse(token).get("token_type", String.class); }

    @Override
    public boolean belongsToSiga(String token) {
        Claims claims = parse(token);
        return "SIGA".equals(claims.get("issuer", String.class))
                && "SIGA_API".equals(claims.get("audience", String.class));
    }

    @Override public Optional<Token> findByTokenHash(String hash) { return tokenRepository.findByTokenHash(hash); }
    @Override public Optional<Token> findByRefreshTokenHash(String hash) { return tokenRepository.findByRefreshTokenHash(hash); }
    @Override public List<Token> findAllValidTokensByUsuario(Usuario usuario) { return tokenRepository.findAllValidTokensByUsuario(usuario); }
    @Override public int consumeRefreshToken(Integer id) { return tokenRepository.consumeRefreshToken(id); }
    @Override public int revokeAllBySessionFamily(String family) { return tokenRepository.revokeAllBySessionFamily(family); }
    @Override public void saveToken(Token token) { tokenRepository.save(token); }

    @Override
    public String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(digest.length * 2);
            for (byte value : digest) result.append(String.format("%02x", value));
            return result.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no está disponible", ex);
        }
    }

    private Claims parse(String token) {
        return Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token).getPayload();
    }

    private SecretKey getSignInKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }
}
