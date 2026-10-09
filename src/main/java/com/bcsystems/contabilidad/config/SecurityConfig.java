package com.bcsystems.contabilidad.config;

import com.bcsystems.contabilidad.auth.JwtAuthenticationFilter;
import com.bcsystems.contabilidad.domain.auth.Token;
import com.bcsystems.contabilidad.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;
    private final TokenService tokenService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, AuthenticationProvider authenticationProvider, TokenService tokenService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationProvider = authenticationProvider;
        this.tokenService = tokenService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(request -> {
                    CorsConfiguration configuration = new CorsConfiguration();
                    configuration.setAllowedOrigins(Arrays.asList("https://admon.omisoltec.mx", "https://shop.omisoltec.mx", "http://127.0.0.1:5501", "http://127.0.0.1:5502"));
                    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
                    configuration.setAllowedHeaders(Arrays.asList("*"));
                    configuration.setExposedHeaders(Arrays.asList("Authorization"));
                    configuration.setAllowCredentials(true);
                    return configuration;
                }))
                 .authorizeHttpRequests(req ->
                         req.requestMatchers("/api/v1/auth/**").permitAll()
                                 .requestMatchers("/api/auth/**").permitAll()
                                 // Legacy modules remain unavailable while the labor phase is developed.
                                 .requestMatchers(
                                         "/api/v1/venta/**",
                                         "/api/v1/compra/**",
                                         "/api/v1/productos/**",
                                         "/api/v1/productos-padre/**",
                                         "/api/v1/variantes/**",
                                         "/api/v1/almacen/**",
                                         "/api/v1/cajas/**",
                                         "/api/v1/cortes/**",
                                         "/api/v1/creditos/**",
                                         "/api/v1/gasto/**",
                                         "/api/v1/pedidos/**",
                                         "/api/v1/sucursales/**",
                                         "/api/v1/clientes/**",
                                         "/api/v1/tipos-pago/**",
                                         "/api/v1/atributos/**",
                                         "/api/v1/proveedor/**",
                                         "/api/v1/proveedores/**",
                                         "/api/v1/kardex/**",
                                         "/api/v1/cuenta-contable/**",
                                         "/api/v1/asiento-contable/**",
                                         "/api/v1/movimiento-contable/**",
                                         "/api/v1/promociones/**"
                                 ).denyAll()
                                 .requestMatchers("/api/inventory/**").hasAnyRole("ADMIN")
                                 .requestMatchers("/api/v1/prestamos/**").hasRole("ADMIN")
                                .requestMatchers("/api/v1/productos/ecommerce/**").permitAll()
                                .requestMatchers("/api/v1/productos/**").hasAnyRole("ADMIN", "ALMACEN")
                                .requestMatchers("/api/v1/productos-padre/**").hasAnyRole("ADMIN", "ALMACEN")
                                .requestMatchers("/api/v1/variantes/disponibles").authenticated()
                                .requestMatchers("/api/v1/variantes/**").hasAnyRole("ADMIN", "ALMACEN")
                                .requestMatchers("/api/v1/almacen/pendientes").hasAnyRole("ADMIN", "ALMACEN")
                                .requestMatchers("/api/v1/almacen/aprobar/**").hasAnyRole("ADMIN", "ALMACEN")
                                .requestMatchers("/api/v1/almacen/rechazar/**").hasAnyRole("ADMIN", "ALMACEN")
                                .requestMatchers("/api/v1/almacen/entrada").hasAnyRole("ADMIN", "ALMACEN")
                                .requestMatchers("/api/v1/almacen/salida-solicitar").authenticated()
                                .requestMatchers("/api/v1/almacen/salida-pdf/**").authenticated()
                                .requestMatchers("/api/v1/almacen/**").authenticated()
                                  .requestMatchers("/api/v1/usuarios/**").hasRole("ADMIN")
                                   .requestMatchers("/api/v1/empleado/**", "/api/v1/nomina/**", "/api/v1/deduccion/**", "/api/v1/comision/**").hasRole("ADMIN")
                                   .requestMatchers("/api/v1/horarios/**", "/api/v1/puntos-geograficos/**", "/api/v1/configuracion/asistencia").hasRole("ADMIN")
                                  .requestMatchers("/api/v1/empleados/*/horario").hasRole("ADMIN")
                                  .requestMatchers("/api/v1/asistencias/evidencias/pendientes", "/api/v1/asistencias/evidencias/*/aprobar", "/api/v1/asistencias/evidencias/*/rechazar", "/api/v1/justificaciones/pendientes", "/api/v1/justificaciones/*/aprobar", "/api/v1/justificaciones/*/rechazar").hasRole("ADMIN")
                                  .requestMatchers("/api/v1/asistencias/**").hasRole("ADMIN")
                                  .requestMatchers("/api/v1/checador/**", "/api/v1/justificaciones/mias").authenticated()
                                  .requestMatchers("/api/v1/justificaciones").authenticated()
                                .requestMatchers("/api/images/**").permitAll()
                                .requestMatchers("/uploads/**").permitAll()
                                .requestMatchers("/error").permitAll()
                                .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout ->
                        logout.logoutUrl("/auth/logout")
                                .addLogoutHandler(this::logout)
                                .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext())
                );

        return http.build();
    }
    private void logout(
            final HttpServletRequest request, final HttpServletResponse response,
            final Authentication authentication
    ) {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        String jwt = authHeader.substring(7);
        Token storedToken = tokenService.findByToken(jwt)
                .orElse(null);
        if (storedToken != null) {
            storedToken.setIsExpired(true);
            storedToken.setIsRevoked(true);
            tokenService.saveToken(storedToken);
            SecurityContextHolder.clearContext();
        }
    }
}
