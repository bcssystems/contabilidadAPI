package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.TipoPago;
import java.util.List;

public interface TipoPagoService {
    List<TipoPago> findAll();
    TipoPago findById(Integer id);
    TipoPago save(TipoPago tipoPago);
    TipoPago update(Integer id, TipoPago tipoPago);
    void delete(Integer id);
}
