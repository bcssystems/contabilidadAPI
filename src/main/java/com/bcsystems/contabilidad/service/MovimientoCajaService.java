package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.MovimientoCaja;
import java.util.List;

public interface MovimientoCajaService {
    List<MovimientoCaja> findByCaja(Integer idCaja);
    MovimientoCaja registrarIngreso(Integer idCaja, Double monto, String motivo, Integer idUsuario);
    MovimientoCaja registrarEgreso(Integer idCaja, Double monto, String motivo, Integer idUsuario);
}
