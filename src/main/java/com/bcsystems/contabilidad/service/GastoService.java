package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Gasto;
import com.bcsystems.contabilidad.domain.Caja;
import com.bcsystems.contabilidad.domain.Persona;
import com.bcsystems.contabilidad.domain.en.EstadoGasto;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.CajaRepository;
import com.bcsystems.contabilidad.repository.GastoRepository;
import com.bcsystems.contabilidad.repository.PersonaRepository;
import jakarta.transaction.Transactional;
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
    public Gasto save(Integer idCaja, String descripcion, Double monto, Integer idUsuario) {
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

        return gastoRepository.save(gasto);
    }

    public List<Gasto> findAll() {
        return gastoRepository.findAll();
    }

    public Page<Gasto> findAllPageable(Pageable pageable) {
        return gastoRepository.findAll(pageable);
    }

    public Gasto findById(Integer id) {
        return gastoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Gasto no encontrado"));
    }

    public List<Gasto> findPendientes() {
        return gastoRepository.findByEstado(EstadoGasto.PENDIENTE);
    }

    public List<Gasto> findByCaja(Integer idCaja) {
        return gastoRepository.findByCajaIdCaja(idCaja);
    }

    @Transactional
    public Gasto autorizar(Integer id, Integer idAutorizador) {
        Gasto gasto = findById(id);
        Persona autorizador = personaRepository.findById(idAutorizador)
                .orElseThrow(() -> new NotFoundException("Autorizador no encontrado"));
        gasto.setEstado(EstadoGasto.AUTORIZADO);
        gasto.setAutorizador(autorizador);
        gasto.setFechaAutorizacion(LocalDateTime.now());
        return gastoRepository.save(gasto);
    }

    @Transactional
    public Gasto rechazar(Integer id, Integer idAutorizador) {
        Gasto gasto = findById(id);
        Persona autorizador = personaRepository.findById(idAutorizador)
                .orElseThrow(() -> new NotFoundException("Autorizador no encontrado"));
        gasto.setEstado(EstadoGasto.RECHAZADO);
        gasto.setAutorizador(autorizador);
        gasto.setFechaAutorizacion(LocalDateTime.now());
        return gastoRepository.save(gasto);
    }

    @Transactional
    public void delete(Integer id) {
        gastoRepository.deleteById(id);
    }
}
