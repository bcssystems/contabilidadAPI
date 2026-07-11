package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.TipoPago;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.TipoPagoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TipoPagoServiceImpl implements TipoPagoService {

    private final TipoPagoRepository tipoPagoRepository;

    public TipoPagoServiceImpl(TipoPagoRepository tipoPagoRepository) {
        this.tipoPagoRepository = tipoPagoRepository;
    }

    @Override
    public List<TipoPago> findAll() {
        return tipoPagoRepository.findByActivoTrue();
    }

    @Override
    public TipoPago findById(Integer id) {
        return tipoPagoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tipo de pago no encontrado"));
    }

    @Override
    @Transactional
    public TipoPago save(TipoPago tipoPago) {
        tipoPago.setActivo(true);
        return tipoPagoRepository.save(tipoPago);
    }

    @Override
    @Transactional
    public TipoPago update(Integer id, TipoPago tipoPago) {
        TipoPago existing = findById(id);
        if (tipoPago.getNombre() != null) existing.setNombre(tipoPago.getNombre());
        return tipoPagoRepository.save(existing);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        TipoPago tipoPago = findById(id);
        tipoPago.setActivo(false);
        tipoPagoRepository.save(tipoPago);
    }
}
