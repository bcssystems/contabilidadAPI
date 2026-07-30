package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Caja;
import com.bcsystems.contabilidad.dto.CajaResponse;
import java.util.List;

public interface CajaService {
    List<CajaResponse> findAll();
    List<CajaResponse> findBySucursal(Integer idSucursal);
    CajaResponse findById(Integer id);
    CajaResponse save(Caja caja);
    CajaResponse update(Integer id, Caja caja);
    void delete(Integer id);
    CajaResponse apertura(Integer id, Double saldoInicial);
    CajaResponse cierre(Integer id);
}
