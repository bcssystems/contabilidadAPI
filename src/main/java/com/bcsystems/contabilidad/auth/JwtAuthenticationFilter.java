package com.bcsystems.contabilidad.auth;

import com.bcsystems.contabilidad.domain.Usuario;
import com.bcsystems.contabilidad.service.TokenService;
import com.bcsystems.contabilidad.service.UsuarioService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import lombok.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserDetailsService userDetailsService;
    private final UsuarioService usuarioService;

    public JwtAuthenticationFilter(TokenService tokenService, UserDetailsService userDetailsService, UsuarioService usuarioService) {
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
        this.usuarioService = usuarioService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();
        if (path.equals("/api/v1/auth/login") || path.equals("/api/v1/auth/register")
                || path.equals("/api/v1/auth/refresh-token") || path.equals("/api/v1/auth/logout")
                || path.equals("/api/v1/auth/csrf")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = cookieValue(request, "SIGA_ACCESS");
        if (token == null || token.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        final String email;
        try { email = tokenService.extractUsername(token); }
        catch (RuntimeException ex) { writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "invalid_token", "Token inválido"); return; }
        final Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();

        if (email == null || existingAuth != null) {
            filterChain.doFilter(request, response);
            return;
        }

        final UserDetails userDetails;
        try {
            userDetails = this.userDetailsService.loadUserByUsername(email);
        } catch (Exception ex) {
            logger.warn("No se pudo cargar UserDetails para email: " + email, ex);
            filterChain.doFilter(request, response);
            return;
        }

        final boolean tokenValidoEnDb = tokenService.findByTokenHash(tokenService.hashToken(token))
                .map(jwt -> !jwt.getIsExpired() && !jwt.getIsRevoked())
                .orElse(false);


        if (tokenValidoEnDb && tokenService.belongsToSiga(token) && "access".equals(tokenService.tokenType(token))) {
            final Optional<Usuario> userOpt = usuarioService.findByUsername(email);

            if (userOpt.isPresent() && Boolean.TRUE.equals(userOpt.get().getActivo())) {
                final boolean isTokenValid = tokenService.isTokenValid(token, userOpt.get());

                if (isTokenValid) {
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                } else {
                    System.out.println("Token no válido según tokenService.isTokenValid");
                }
            } else {
                System.out.println("Trabajador no encontrado para email: " + email);
            }
        } else {
            System.out.println("Token no encontrado en DB o está expirado/revocado");
        }

        filterChain.doFilter(request, response);
    }

    private String cookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) if (name.equals(cookie.getName())) return cookie.getValue();
        return null;
    }

    private void writeError(HttpServletResponse response, int status, String error, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + error + "\",\"message\":\"" + message + "\"}");
    }

}
