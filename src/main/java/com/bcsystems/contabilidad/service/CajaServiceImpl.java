package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Caja;
import com.bcsystems.contabilidad.domain.en.CajaEstado;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.CajaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CajaServiceImpl implements CajaService {

    private final CajaRepository cajaRepository;

    public CajaServiceImpl(CajaRepository cajaRepository) {
        this.cajaRepository = cajaRepository;
    }

    @Override
    public List<Caja> findAll() {
        return cajaRepository.findByActivaTrue();
    }

    @Override
    public List<Caja> findBySucursal(Integer idSucursal) {
        return cajaRepository.findBySucursalIdSucursalAndActivaTrue(idSucursal);
    }

    @Override
    public Caja findById(Integer id) {
        return cajaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Caja no encontrada"));
    }

    @Override
    @Transactional
    public Caja save(Caja caja) {
        caja.setActiva(true);
        caja.setEstado(CajaEstado.CERRADA);
        caja.setSaldoActual(0.0);
        return cajaRepository.save(caja);
    }

    @Override
    @Transactional
    public Caja update(Integer id, Caja caja) {
        Caja existing = findById(id);
        if (caja.getNombre() != null) existing.setNombre(caja.getNombre());
        if (caja.getSucursal() != null) existing.setSucursal(caja.getSucursal());
        return cajaRepository.save(existing);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        Caja caja = findById(id);
        caja.setActiva(false);
        cajaRepository.save(caja);
    }

    @Override
    @Transactional
    public Caja apertura(Integer id, Double saldoInicial) {
        Caja caja = findById(id);
        caja.setEstado(CajaEstado.ABIERTA);
        caja.setSaldoActual(saldoInicial != null ? saldoInicial : 0.0);
        caja.setFechaApertura(LocalDateTime.now());
        caja.setFechaCierre(null);
        return cajaRepository.save(caja);
    }

    @Override
    @Transactional
    public Caja cierre(Integer id) {
        Caja caja = findById(id);
        caja.setEstado(CajaEstado.CERRADA);
        caja.setFechaCierre(LocalDateTime.now());
        return cajaRepository.save(caja);
    }
}
