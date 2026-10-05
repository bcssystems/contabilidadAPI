package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.dto.UserDetailedResponse;
import com.bcsystems.contabilidad.dto.UserRegisterRequest;
import com.bcsystems.contabilidad.dto.UserUpdateRequest;
import com.bcsystems.contabilidad.service.AuthService;
import com.bcsystems.contabilidad.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final AuthService authService;

    public UsuarioController(UsuarioService usuarioService, AuthService authService) {
        this.usuarioService = usuarioService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<List<UserDetailedResponse>> findAll() {
        return ResponseEntity.ok(usuarioService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDetailedResponse> findById(@PathVariable Integer id) {
        return usuarioService.findById(id)
                .map(u -> new UserDetailedResponse(
                        u.getIdUser(), u.getUsername(), u.getRole().name(),
                        u.getPersona().getNombre(), u.getPersona().getPrimerApellido(),
                        u.getPersona().getSegundoApellido(),
                        u.getPersona().getPersonType().name(),
                        u.getActivo(), u.getPersona().getFechaRegistro(),
                        u.getPosition() != null ? u.getPosition().name() : null,
                        u.getSueldoBase(), u.getSueldoSemanal()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody UserRegisterRequest request) {
        try {
            authService.register(request);
            Integer id = usuarioService.findByUsername(request.username()).orElseThrow().getIdUser();
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id, "message", "Usuario creado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error al crear usuario: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody UserUpdateRequest request) {
        try {
            usuarioService.updateUser(id, request);
            return ResponseEntity.ok(Map.of("message", "Usuario actualizado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error al actualizar usuario: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            usuarioService.updateUser(id, new UserUpdateRequest(null, null, null, null, null, false, null, null, null));
            return ResponseEntity.ok(Map.of("message", "Usuario desactivado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error al desactivar usuario: " + e.getMessage()));
        }
    }
}
