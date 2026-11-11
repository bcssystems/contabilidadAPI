package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Deduccion;
import com.bcsystems.contabilidad.domain.DeduccionFija;
import com.bcsystems.contabilidad.domain.Usuario;
import com.bcsystems.contabilidad.domain.en.EstadoDeduccion;
import com.bcsystems.contabilidad.domain.en.OrigenDeduccion;
import com.bcsystems.contabilidad.domain.en.TipoDeduccion;
import com.bcsystems.contabilidad.dto.DeduccionFijaRequest;
import com.bcsystems.contabilidad.dto.DeduccionFijaResponse;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.DeduccionFijaRepository;
import com.bcsystems.contabilidad.repository.DeduccionRepository;
import com.bcsystems.contabilidad.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeduccionFijaService {
    private final DeduccionFijaRepository fixedRepository;
    private final DeduccionRepository deductionRepository;
    private final UsuarioRepository userRepository;

    public DeduccionFijaService(DeduccionFijaRepository fixedRepository,
                                DeduccionRepository deductionRepository,
                                UsuarioRepository userRepository) {
        this.fixedRepository = fixedRepository;
        this.deductionRepository = deductionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public List<DeduccionFijaResponse> findAll() {
        return fixedRepository.findAll().stream().map(DeduccionFijaResponse::fromEntity).toList();
    }

    @Transactional
    public DeduccionFijaResponse findById(Long id) {
        return fixedRepository.findById(id).map(DeduccionFijaResponse::fromEntity)
                .orElseThrow(() -> new NotFoundException("Deducción fija no encontrada"));
    }

    @Transactional
    public DeduccionFijaResponse save(DeduccionFijaRequest request) {
        validate(request);
        DeduccionFija item = new DeduccionFija();
        apply(item, request);
        return DeduccionFijaResponse.fromEntity(fixedRepository.save(item));
    }

    @Transactional
    public DeduccionFijaResponse update(Long id, DeduccionFijaRequest request) {
        validate(request);
        DeduccionFija item = fixedRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Deducción fija no encontrada"));
        apply(item, request);
        return DeduccionFijaResponse.fromEntity(fixedRepository.save(item));
    }

    @Transactional
    public void delete(Long id) {
        DeduccionFija item = fixedRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Deducción fija no encontrada"));
        item.setActivo(false);
        fixedRepository.save(item);
    }

    @Transactional
    public void generarParaNomina(Usuario usuario, LocalDate inicio, LocalDate fin) {
        fixedRepository.findAll().stream()
                .filter(item -> Boolean.TRUE.equals(item.getActivo()))
                .filter(item -> !item.getFechaInicio().isAfter(fin)
                        && (item.getFechaFin() == null || !item.getFechaFin().isBefore(inicio)))
                .filter(item -> item.getUsuarios().stream().anyMatch(u -> u.getIdUser().equals(usuario.getIdUser())))
                .forEach(item -> {
                    if (deductionRepository.existsByDeduccionFijaIdDeduccionFijaAndUsuarioIdUserAndPeriodoInicioAndPeriodoFin(
                            item.getIdDeduccionFija(), usuario.getIdUser(), inicio, fin)) return;
                    deductionRepository.save(Deduccion.builder()
                            .usuario(usuario)
                            .concepto(item.getConcepto())
                            .monto(item.getMonto())
                            .fecha(fin)
                            .estado(EstadoDeduccion.PENDIENTE)
                            .tipo(TipoDeduccion.FIJA)
                            .origen(OrigenDeduccion.FIJA)
                            .deduccionFija(item)
                            .periodoInicio(inicio)
                            .periodoFin(fin)
                            .build());
                });
    }

    private void validate(DeduccionFijaRequest request) {
        if (request == null || request.concepto() == null || request.concepto().isBlank()
                || request.monto() == null || request.monto().signum() <= 0
                || request.fechaInicio() == null || (request.fechaFin() != null && request.fechaFin().isBefore(request.fechaInicio()))) {
            throw new IllegalArgumentException("Concepto, monto y fecha de inicio son obligatorios; la vigencia no es válida");
        }
        if (request.usuarioIds() == null || request.usuarioIds().isEmpty()) {
            throw new IllegalArgumentException("Selecciona al menos un usuario");
        }
    }

    private void apply(DeduccionFija item, DeduccionFijaRequest request) {
        item.setConcepto(request.concepto().trim());
        item.setMonto(request.monto());
        item.setActivo(request.activo() == null || request.activo());
        item.setFechaInicio(request.fechaInicio());
        item.setFechaFin(request.fechaFin());
        item.setUsuarios(request.usuarioIds().stream().map(id -> userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + id))).collect(Collectors.toSet()));
    }
}
