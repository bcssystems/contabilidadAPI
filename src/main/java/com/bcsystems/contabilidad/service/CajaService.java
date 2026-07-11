package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Caja;
import java.util.List;

public interface CajaService {
    List<Caja> findAll();
    List<Caja> findBySucursal(Integer idSucursal);
    Caja findById(Integer id);
    Caja save(Caja caja);
    Caja update(Integer id, Caja caja);
    void delete(Integer id);
    Caja apertura(Integer id, Double saldoInicial);
    Caja cierre(Integer id);
}
