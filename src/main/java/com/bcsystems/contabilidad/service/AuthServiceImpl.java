package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Persona;
import com.bcsystems.contabilidad.domain.Empleado;
import com.bcsystems.contabilidad.domain.Usuario;
import com.bcsystems.contabilidad.domain.auth.Token;
import com.bcsystems.contabilidad.domain.en.Role;
import com.bcsystems.contabilidad.domain.en.TokenType;
import com.bcsystems.contabilidad.dto.TokenResponse;
import com.bcsystems.contabilidad.dto.UserAuthRequest;
import com.bcsystems.contabilidad.dto.UserRegisterRequest;
import com.bcsystems.contabilidad.exception.InvalidEntryException;
import com.bcsystems.contabilidad.repository.EmpleadoRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private final TokenService tokenService;
    private final UsuarioService usuarioService;
    private final PersonaService personaService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmpleadoRepository empleadoRepository;

    public AuthServiceImpl(TokenService tokenService, UsuarioService usuarioService, PersonaService personaService, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, EmpleadoRepository empleadoRepository) {
        this.tokenService = tokenService;
        this.usuarioService = usuarioService;
        this.personaService = personaService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.empleadoRepository = empleadoRepository;
    }


    @Transactional
    public TokenResponse register(UserRegisterRequest userRegisterRequest) {
        Persona persona = Persona
                .builder()
                .nombre(userRegisterRequest.nombre())
                .primerApellido(userRegisterRequest.primerApellido())
                .segundoApellido(userRegisterRequest.segundoApellido())
                .personType(userRegisterRequest.personType())
                .build();
        Persona personaSaved = personaService.save(persona);
        Usuario usuario = Usuario
                .builder()
                .username(userRegisterRequest.username())
                .password(passwordEncoder.encode(userRegisterRequest.password()))
                .role(userRegisterRequest.role() != null ? userRegisterRequest.role() : Role.USER)
                .activo(true)
                .persona(personaSaved)
                .build();
        Usuario savedUsuario = usuarioService.save(usuario);
        if (empleadoRepository.findByUsuarioIdUser(savedUsuario.getIdUser()).isEmpty()) {
            Empleado empleado = empleadoRepository.findByPersonaIdPerson(personaSaved.getIdPerson()).orElseGet(() -> Empleado.builder()
                    .persona(personaSaved)
                    .activo(true)
                    .sueldoBase(java.math.BigDecimal.ZERO)
                    .sueldoSemanal(java.math.BigDecimal.ZERO)
                    .build());
            empleado.setUsuario(savedUsuario);
            if (empleado.getSueldoSemanal() == null) empleado.setSueldoSemanal(java.math.BigDecimal.ZERO);
            if (empleado.getSueldoBase() == null) empleado.setSueldoBase(java.math.BigDecimal.ZERO);
            empleadoRepository.save(empleado);
        }
        String token = tokenService.generateToken(usuario);
        String refreshToken = tokenService.generateRefreshToken(usuario);

        saveUserToken(usuario, token);
        return new TokenResponse(token, refreshToken);
    }

    @Override
    public TokenResponse login(UserAuthRequest userAuthRequest) {
        try{
            authenticationManager
                    .authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    userAuthRequest.email(),
                                    userAuthRequest.password()
                            )
                    );
        } catch (InvalidEntryException ex){
            throw new InvalidEntryException("Credenciales invalidas");
        }

        Optional<Usuario> usuario = usuarioService.findByUsername(userAuthRequest.email());
        String accessToken = tokenService.generateToken(usuario.get());
        String refreshToken = tokenService.generateRefreshToken(usuario.get());
        removeAllUserTokens(usuario.get());
        saveUserToken(usuario.get(), accessToken);
        return new TokenResponse(accessToken, refreshToken);
    }

    @Override
    public void saveUserToken(Usuario usuario, String token) {
        Token tokenJwt = Token
                .builder()
                .token(token)
                .isExpired(false)
                .isRevoked(false)
                .type(TokenType.BEARER)
                .usuario(usuario)
                .build();
        tokenService.saveToken(tokenJwt);
    }

    @Override
    public void removeAllUserTokens(Usuario usuario) {
        List<Token> tokens = tokenService.findalltokensbyusuario(usuario);
        if(!tokens.isEmpty()){
            tokenService.deleteTokens(tokens);
        }
    }

    @Override
    public TokenResponse refreshToken(String authentication) {
        if (authentication == null || !authentication.startsWith("Bearer ")) {
            throw new InvalidEntryException("Error de entrada de token");
        }
        String refreshToken = authentication.substring(7);
        String email = tokenService.extractUsername(refreshToken);
        if (email == null) {
            return null;
        }

        Usuario usuario = usuarioService.findByUsername(email).orElseThrow();
        boolean isTokenValid = tokenService.isTokenValid(refreshToken, usuario);
        if (!isTokenValid) {
            return null;
        }

        String accessToken = tokenService.generateToken(usuario);
        removeAllUserTokens(usuario);
        saveUserToken(usuario, accessToken);

        return new TokenResponse(accessToken, refreshToken);
    }
}
