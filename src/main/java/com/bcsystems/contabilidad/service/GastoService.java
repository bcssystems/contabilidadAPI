package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Gasto;
import com.bcsystems.contabilidad.domain.Caja;
import com.bcsystems.contabilidad.domain.Persona;
import com.bcsystems.contabilidad.domain.en.EstadoGasto;
import com.bcsystems.contabilidad.dto.GastoResponse;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.CajaRepository;
import com.bcsystems.contabilidad.repository.GastoRepository;
import com.bcsystems.contabilidad.repository.PersonaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GastoService {

    private final GastoRepository gastoRepository;
    private final CajaRepository cajaRepository;
    private final PersonaRepository personaRepository;

    public GastoService(GastoRepository gastoRepository,
                        CajaRepository cajaRepository,
                        PersonaRepository personaRepository) {
        this.gastoRepository = gastoRepository;
        this.cajaRepository = cajaRepository;
        this.personaRepository = personaRepository;
    }

    @Transactional
    public GastoResponse save(Integer idCaja, String descripcion, Double monto, Integer idUsuario) {
        Caja caja = cajaRepository.findById(idCaja)
                .orElseThrow(() -> new NotFoundException("Caja no encontrada"));
        Persona usuario = personaRepository.findById(idUsuario)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        Gasto gasto = Gasto.builder()
                .caja(caja)
                .descripcion(descripcion)
                .monto(monto)
                .usuario(usuario)
                .estado(EstadoGasto.PENDIENTE)
                .fechaCreacion(LocalDateTime.now())
                .build();

        return GastoResponse.fromEntity(gastoRepository.save(gasto));
    }

    @Transactional(readOnly = true)
    public List<GastoResponse> findAll() {
        return gastoRepository.findAll().stream()
                .map(GastoResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<GastoResponse> findAllPageable(Pageable pageable) {
        return gastoRepository.findAll(pageable)
                .map(GastoResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public GastoResponse findById(Integer id) {
        return gastoRepository.findById(id)
                .map(GastoResponse::fromEntity)
                .orElseThrow(() -> new NotFoundException("Gasto no encontrado"));
    }

    @Transactional(readOnly = true)
    public List<GastoResponse> findPendientes() {
        return gastoRepository.findByEstado(EstadoGasto.PENDIENTE).stream()
                .map(GastoResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GastoResponse> findByCaja(Integer idCaja) {
        return gastoRepository.findByCajaIdCaja(idCaja).stream()
                .map(GastoResponse::fromEntity)
                .toList();
    }

    @Transactional
    public GastoResponse autorizar(Integer id, Integer idAutorizador) {
        Gasto gasto = gastoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Gasto no encontrado"));
        Persona autorizador = personaRepository.findById(idAutorizador)
                .orElseThrow(() -> new NotFoundException("Autorizador no encontrado"));
        gasto.setEstado(EstadoGasto.AUTORIZADO);
        gasto.setAutorizador(autorizador);
        gasto.setFechaAutorizacion(LocalDateTime.now());
        return GastoResponse.fromEntity(gastoRepository.save(gasto));
    }

    @Transactional
    public GastoResponse rechazar(Integer id, Integer idAutorizador) {
        Gasto gasto = gastoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Gasto no encontrado"));
        Persona autorizador = personaRepository.findById(idAutorizador)
                .orElseThrow(() -> new NotFoundException("Autorizador no encontrado"));
        gasto.setEstado(EstadoGasto.RECHAZADO);
        gasto.setAutorizador(autorizador);
        gasto.setFechaAutorizacion(LocalDateTime.now());
        return GastoResponse.fromEntity(gastoRepository.save(gasto));
    }

    @Transactional
    public void delete(Integer id) {
        gastoRepository.deleteById(id);
    }
}
