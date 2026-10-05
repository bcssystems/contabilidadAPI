package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.ConfiguracionAsistencia;
import com.bcsystems.contabilidad.dto.ConfiguracionAsistenciaRequest;
import com.bcsystems.contabilidad.repository.ConfiguracionAsistenciaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfiguracionAsistenciaService {
    private static final int ID = 1;
    private final ConfiguracionAsistenciaRepository repository;
    public ConfiguracionAsistenciaService(ConfiguracionAsistenciaRepository repository) { this.repository = repository; }
    public ConfiguracionAsistencia get() {
        return repository.findById(ID).orElseGet(() -> repository.save(defaults()));
    }
    @Transactional public ConfiguracionAsistencia update(ConfiguracionAsistenciaRequest request) {
        ConfiguracionAsistencia value = get();
        value.setToleranciaEntrada(nonNegative(request.toleranciaEntrada(), 0));
        value.setToleranciaSalida(nonNegative(request.toleranciaSalida(), 0));
        value.setRadioMaximo(nonNegative(request.radioMaximo(), 100));
        value.setRequiereSelfie(request.requiereSelfie() == null || request.requiereSelfie());
        return repository.save(value);
    }
    private ConfiguracionAsistencia defaults() { return ConfiguracionAsistencia.builder().id(ID).toleranciaEntrada(0).toleranciaSalida(0).radioMaximo(100).requiereSelfie(true).build(); }
    private int nonNegative(Integer value, int fallback) { return value == null ? fallback : Math.max(0, value); }
}
