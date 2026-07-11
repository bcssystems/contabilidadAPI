package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Abono;
import com.bcsystems.contabilidad.domain.Credito;
import java.util.List;
import java.util.Map;

public interface CreditoService {
    List<Credito> findByCliente(Integer idCliente);
    Credito findById(Integer id);
    Credito save(Credito credito);
    Abono registrarAbono(Integer idCredito, Double monto, String tipo, Integer idUsuario);
    Abono registrarAbonoGeneral(Integer idCliente, Double monto, Integer idUsuario);
    List<Map<String, Object>> getMovimientosCliente(Integer idCliente);
}
