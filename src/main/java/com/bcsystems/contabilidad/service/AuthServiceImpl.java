package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Persona;
import com.bcsystems.contabilidad.domain.Usuario;
import com.bcsystems.contabilidad.domain.auth.Token;
import com.bcsystems.contabilidad.domain.en.Role;
import com.bcsystems.contabilidad.dto.TokenResponse;
import com.bcsystems.contabilidad.dto.UserAuthRequest;
import com.bcsystems.contabilidad.dto.UserRegisterRequest;
import com.bcsystems.contabilidad.exception.InvalidEntryException;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {
    private final TokenService tokenService;
    private final UsuarioService usuarioService;
    private final PersonaService personaService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(TokenService tokenService, UsuarioService usuarioService, PersonaService personaService,
                           PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.tokenService = tokenService;
        this.usuarioService = usuarioService;
        this.personaService = personaService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @Override
    @Transactional
    public TokenResponse register(UserRegisterRequest request) {
        Persona persona = Persona.builder().nombre(request.nombre()).primerApellido(request.primerApellido())
                .segundoApellido(request.segundoApellido()).personType(request.personType()).build();
        persona = personaService.save(persona);
        Usuario usuario = Usuario.builder()
                .username(request.username() == null || request.username().isBlank()
                        ? usuarioService.generateUsername(request.nombre(), request.primerApellido(), request.segundoApellido())
                        : request.username().trim())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role() == null ? Role.USER : request.role())
                .activo(true).persona(persona).position(request.position())
                .sueldoBase(request.sueldoBase() == null ? java.math.BigDecimal.ZERO : request.sueldoBase())
                .sueldoSemanal(request.sueldoSemanal() == null ? request.sueldoBase() : request.sueldoSemanal())
                .sueldoHora(java.math.BigDecimal.ZERO).build();
        usuario = usuarioService.save(usuario);
        revokeAllTokens(usuario);
        return issueTokens(usuario, UUID.randomUUID().toString());
    }

    @Override
    @Transactional
    public TokenResponse login(UserAuthRequest request) {
        String username = request.email() == null ? "" : request.email().trim();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, request.password()));
        Usuario usuario = usuarioService.findByUsername(username)
                .orElseThrow(() -> new InvalidEntryException("Credenciales inválidas"));
        revokeAllTokens(usuario);
        return issueTokens(usuario, UUID.randomUUID().toString());
    }

    @Override
    @Transactional
    public TokenResponse refreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) throw new InvalidEntryException("Token de refresco inválido");
        final String username;
        try { username = tokenService.extractUsername(refreshToken); }
        catch (RuntimeException ex) { throw new InvalidEntryException("Token de refresco inválido"); }
        Usuario usuario = usuarioService.findByUsername(username)
                .orElseThrow(() -> new InvalidEntryException("Usuario no encontrado"));
        Token stored = tokenService.findByRefreshTokenHash(tokenService.hashToken(refreshToken)).orElse(null);
        if (stored == null || Boolean.TRUE.equals(stored.getIsExpired()) || Boolean.TRUE.equals(stored.getIsRevoked())) {
            if (stored != null && stored.getSessionFamily() != null) tokenService.revokeAllBySessionFamily(stored.getSessionFamily());
            throw new InvalidEntryException("Token de refresco inválido o reutilizado");
        }
        if (!tokenService.belongsToSiga(refreshToken) || !"refresh".equals(tokenService.tokenType(refreshToken))
                || !tokenService.isTokenValid(refreshToken, usuario) || tokenService.consumeRefreshToken(stored.getIdToken()) == 0) {
            tokenService.revokeAllBySessionFamily(stored.getSessionFamily());
            throw new InvalidEntryException("Token de refresco inválido o reutilizado");
        }
        revokeAllTokens(usuario);
        return issueTokens(usuario, stored.getSessionFamily());
    }

    @Override
    @Transactional
    public void logout(String accessToken, String refreshToken) {
        Token stored = refreshToken == null ? null : tokenService.findByRefreshTokenHash(tokenService.hashToken(refreshToken)).orElse(null);
        if (stored == null && accessToken != null) stored = tokenService.findByTokenHash(tokenService.hashToken(accessToken)).orElse(null);
        if (stored != null) tokenService.revokeAllBySessionFamily(stored.getSessionFamily());
    }

    private TokenResponse issueTokens(Usuario usuario, String family) {
        Token token = tokenService.generateTokenPair(usuario, family);
        String accessToken = token.getToken();
        String refreshToken = token.getRefreshToken();
        token.setToken(null);
        token.setRefreshToken(null);
        tokenService.saveToken(token);
        return new TokenResponse(accessToken, refreshToken);
    }

    private void revokeAllTokens(Usuario usuario) {
        List<Token> tokens = tokenService.findAllValidTokensByUsuario(usuario);
        tokens.forEach(token -> { token.setIsExpired(true); token.setIsRevoked(true); });
        tokens.forEach(tokenService::saveToken);
    }
}
