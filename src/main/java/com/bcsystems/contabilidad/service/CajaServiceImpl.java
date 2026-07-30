package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Caja;
import com.bcsystems.contabilidad.domain.en.CajaEstado;
import com.bcsystems.contabilidad.dto.CajaResponse;
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
    @Transactional(readOnly = true)
    public List<CajaResponse> findAll() {
        return cajaRepository.findByActivaTrue().stream()
                .map(CajaResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CajaResponse> findBySucursal(Integer idSucursal) {
        return cajaRepository.findBySucursalIdSucursalAndActivaTrue(idSucursal).stream()
                .map(CajaResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CajaResponse findById(Integer id) {
        return cajaRepository.findById(id)
                .map(CajaResponse::fromEntity)
                .orElseThrow(() -> new NotFoundException("Caja no encontrada"));
    }

    @Override
    @Transactional
    public CajaResponse save(Caja caja) {
        caja.setActiva(true);
        caja.setEstado(CajaEstado.CERRADA);
        caja.setSaldoActual(0.0);
        return CajaResponse.fromEntity(cajaRepository.save(caja));
    }

    @Override
    @Transactional
    public CajaResponse update(Integer id, Caja caja) {
        Caja existing = cajaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Caja no encontrada"));
        if (caja.getNombre() != null) existing.setNombre(caja.getNombre());
        if (caja.getSucursal() != null) existing.setSucursal(caja.getSucursal());
        return CajaResponse.fromEntity(cajaRepository.save(existing));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        Caja caja = cajaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Caja no encontrada"));
        caja.setActiva(false);
        cajaRepository.save(caja);
    }

    @Override
    @Transactional
    public CajaResponse apertura(Integer id, Double saldoInicial) {
        Caja caja = cajaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Caja no encontrada"));
        caja.setEstado(CajaEstado.ABIERTA);
        caja.setSaldoActual(saldoInicial != null ? saldoInicial : 0.0);
        caja.setFechaApertura(LocalDateTime.now());
        caja.setFechaCierre(null);
        return CajaResponse.fromEntity(cajaRepository.save(caja));
    }

    @Override
    @Transactional
    public CajaResponse cierre(Integer id) {
        Caja caja = cajaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Caja no encontrada"));
        caja.setEstado(CajaEstado.CERRADA);
        caja.setFechaCierre(LocalDateTime.now());
        return CajaResponse.fromEntity(cajaRepository.save(caja));
    }
}
