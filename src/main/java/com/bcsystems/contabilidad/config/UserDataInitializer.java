package com.bcsystems.contabilidad.config;

import com.bcsystems.contabilidad.domain.Persona;
import com.bcsystems.contabilidad.domain.Usuario;
import com.bcsystems.contabilidad.domain.en.PersonType;
import com.bcsystems.contabilidad.domain.en.Role;
import com.bcsystems.contabilidad.repository.PersonaRepository;
import com.bcsystems.contabilidad.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserDataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PersonaRepository personaRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDataInitializer(UsuarioRepository usuarioRepository, PersonaRepository personaRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.personaRepository = personaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            return;
        }

        Persona persona = Persona.builder()
                .nombre("Admin")
                .primerApellido("Sistema")
                .personType(PersonType.USUARIO)
                .build();
        personaRepository.save(persona);

        Usuario usuario = Usuario.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .activo(true)
                .persona(persona)
                .build();
        usuarioRepository.save(usuario);

        System.out.println("Usuario admin creado: admin / admin123");
    }
}
