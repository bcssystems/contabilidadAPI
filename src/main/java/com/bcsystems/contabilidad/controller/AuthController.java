package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.dto.TokenResponse;
import com.bcsystems.contabilidad.dto.UserAuthRequest;
import com.bcsystems.contabilidad.dto.UserDetailedResponse;
import com.bcsystems.contabilidad.dto.UserRegisterRequest;
import com.bcsystems.contabilidad.service.AuthService;
import com.bcsystems.contabilidad.service.TokenService;
import com.bcsystems.contabilidad.service.UsuarioService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;
    private final TokenService tokenService;

    public AuthController(AuthService authService, UsuarioService usuarioService, TokenService tokenService) {
        this.authService = authService;
        this.usuarioService = usuarioService;
        this.tokenService = tokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegisterRequest userRegisterRequest) {
        TokenResponse tokenResponse = authService.register(userRegisterRequest);
        return ResponseEntity.status(HttpStatus.OK).body(tokenResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserAuthRequest userAuthRequest) {
        TokenResponse tokenResponse = authService.login(userAuthRequest);
        return ResponseEntity.status(HttpStatus.OK).body(tokenResponse);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.refreshToken(token));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader(HttpHeaders.AUTHORIZATION) String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token no proporcionado");
        }
        String jwt = token.substring(7);
        String username = tokenService.extractUsername(jwt);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido");
        }
        UserDetailedResponse user = usuarioService.findByUsernameDetailed(username);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }
}
