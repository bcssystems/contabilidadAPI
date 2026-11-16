package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.dto.TokenResponse;
import com.bcsystems.contabilidad.dto.UserAuthRequest;
import com.bcsystems.contabilidad.dto.UserDetailedResponse;
import com.bcsystems.contabilidad.dto.UserRegisterRequest;
import com.bcsystems.contabilidad.auth.LoginRateLimiter;
import com.bcsystems.contabilidad.service.AuthService;
import com.bcsystems.contabilidad.service.TokenService;
import com.bcsystems.contabilidad.service.UsuarioService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final UsuarioService usuarioService;
    private final TokenService tokenService;
    private final boolean secureCookies;
    private final String sameSite;
    private final LoginRateLimiter loginRateLimiter;

    public AuthController(AuthService authService, UsuarioService usuarioService, TokenService tokenService,
                          @Value("${app.security.cookies.secure:false}") boolean secureCookies,
                          @Value("${app.security.cookies.same-site:Lax}") String sameSite,
                          LoginRateLimiter loginRateLimiter) {
        this.authService = authService;
        this.usuarioService = usuarioService;
        this.tokenService = tokenService;
        this.secureCookies = secureCookies;
        this.sameSite = sameSite;
        this.loginRateLimiter = loginRateLimiter;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegisterRequest request, HttpServletResponse response) {
        return authenticatedResponse(authService.register(request), response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserAuthRequest request, HttpServletRequest httpRequest, HttpServletResponse response) {
        String key = httpRequest.getRemoteAddr() + ":" + (request.email() == null ? "" : request.email().trim().toLowerCase());
        if (!loginRateLimiter.isAllowed(key)) return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        try {
            ResponseEntity<?> result = authenticatedResponse(authService.login(request), response);
            loginRateLimiter.clear(key);
            return result;
        } catch (RuntimeException ex) {
            loginRateLimiter.recordFailure(key);
            throw ex;
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@CookieValue(value = "SIGA_REFRESH", required = false) String refreshToken,
                                          HttpServletResponse response) {
        return authenticatedResponse(authService.refreshToken(refreshToken), response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(value = "SIGA_ACCESS", required = false) String accessToken,
                                       @CookieValue(value = "SIGA_REFRESH", required = false) String refreshToken,
                                       HttpServletResponse response) {
        authService.logout(accessToken, refreshToken);
        deleteCookies(response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserDetailedResponse> me(@CookieValue(value = "SIGA_ACCESS", required = false) String accessToken) {
        if (accessToken == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        try {
            String username = tokenService.extractUsername(accessToken);
            if (!Boolean.TRUE.equals(usuarioService.findByUsername(username).get().getActivo())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            return ResponseEntity.ok(usuarioService.findByUsernameDetailed(username));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/csrf")
    public ResponseEntity<String> csrf(CsrfToken token) {
        return ResponseEntity.ok("{\"token\":\"" + token.getToken() + "\"}");
    }

    private ResponseEntity<?> authenticatedResponse(TokenResponse tokens, HttpServletResponse response) {
        addCookie(response, "SIGA_ACCESS", tokens.accessToken(), "/", 900);
        addCookie(response, "SIGA_REFRESH", tokens.refreshToken(), "/api/v1/auth", 604800);
        return ResponseEntity.ok().build();
    }

    private void addCookie(HttpServletResponse response, String name, String value, String path, long maxAge) {
        response.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from(name, value).httpOnly(true)
                .secure(secureCookies).sameSite(sameSite).path(path).maxAge(maxAge).build().toString());
    }

    private void deleteCookies(HttpServletResponse response) {
        addCookie(response, "SIGA_ACCESS", "", "/", 0);
        addCookie(response, "SIGA_REFRESH", "", "/api/v1/auth", 0);
    }
}
