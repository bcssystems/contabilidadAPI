package com.bcsystems.contabilidad.auth;

import com.bcsystems.contabilidad.domain.Usuario;
import com.bcsystems.contabilidad.service.TokenService;
import com.bcsystems.contabilidad.service.UsuarioService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

        if (request.getServletPath().contains("/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authorizationHeader.substring(7);
        final String email = tokenService.extractUsername(token);
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

        final boolean tokenValidoEnDb = tokenService.findByToken(token)
                .map(jwt -> !jwt.getIsExpired() && !jwt.getIsRevoked())
                .orElse(false);


        if (tokenValidoEnDb) {
            final Optional<Usuario> userOpt = usuarioService.findByUsername(email);

            if (userOpt.isPresent()) {
                final boolean isTokenValid = tokenService.isTokenValid(token, userOpt.get());

                if (isTokenValid) {
                    Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

                    if (authorities == null || authorities.isEmpty()) {
                        String rolEntidad = null;
                        if (userOpt.get().getPersona() != null) {
                            rolEntidad = String.valueOf(userOpt.get().getRole());
                        }

                        if (rolEntidad != null) {
                            String authorityString = rolEntidad.startsWith("ROLE_") ? rolEntidad : "ROLE_" + rolEntidad;
                            authorities = List.of(new SimpleGrantedAuthority(authorityString));
                        } else {
                            authorities = List.of();
                        }
                    }

                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            authorities
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

}
