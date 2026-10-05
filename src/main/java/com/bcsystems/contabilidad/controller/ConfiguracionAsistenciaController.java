package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.domain.ConfiguracionAsistencia;
import com.bcsystems.contabilidad.dto.ConfiguracionAsistenciaRequest;
import com.bcsystems.contabilidad.service.ConfiguracionAsistenciaService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/configuracion/asistencia")
public class ConfiguracionAsistenciaController {
    private final ConfiguracionAsistenciaService service;
    public ConfiguracionAsistenciaController(ConfiguracionAsistenciaService service) { this.service = service; }
    @GetMapping public ConfiguracionAsistencia get() { return service.get(); }
    @PutMapping public ConfiguracionAsistencia update(@RequestBody ConfiguracionAsistenciaRequest request) { return service.update(request); }
}
