package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.*;
import com.bcsystems.contabilidad.domain.en.TipoMovimientoCaja;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.CajaRepository;
import com.bcsystems.contabilidad.repository.MovimientoCajaRepository;
import com.bcsystems.contabilidad.repository.PersonaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MovimientoCajaServiceImpl implements MovimientoCajaService {

    private final MovimientoCajaRepository movimientoCajaRepository;
    private final CajaRepository cajaRepository;
    private final PersonaRepository personaRepository;

    public MovimientoCajaServiceImpl(MovimientoCajaRepository movimientoCajaRepository,
                                     CajaRepository cajaRepository,
                                     PersonaRepository personaRepository) {
        this.movimientoCajaRepository = movimientoCajaRepository;
        this.cajaRepository = cajaRepository;
        this.personaRepository = personaRepository;
    }

    @Override
    public List<MovimientoCaja> findByCaja(Integer idCaja) {
        return movimientoCajaRepository.findByCajaIdCajaOrderByFechaDesc(idCaja);
    }

    @Override
    @Transactional
    public MovimientoCaja registrarIngreso(Integer idCaja, Double monto, String motivo, Integer idUsuario) {
        return registrarMovimiento(idCaja, TipoMovimientoCaja.INGRESO, monto, motivo, idUsuario);
    }

    @Override
    @Transactional
    public MovimientoCaja registrarEgreso(Integer idCaja, Double monto, String motivo, Integer idUsuario) {
        return registrarMovimiento(idCaja, TipoMovimientoCaja.EGRESO, monto, motivo, idUsuario);
    }

    private MovimientoCaja registrarMovimiento(Integer idCaja, TipoMovimientoCaja tipo, Double monto, String motivo, Integer idUsuario) {
        Caja caja = cajaRepository.findById(idCaja)
                .orElseThrow(() -> new NotFoundException("Caja no encontrada"));
        Persona usuario = personaRepository.findById(idUsuario)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        MovimientoCaja mov = MovimientoCaja.builder()
                .caja(caja)
                .tipo(tipo)
                .monto(monto)
                .motivo(motivo)
                .usuario(usuario)
                .fecha(LocalDateTime.now())
                .build();
        mov = movimientoCajaRepository.save(mov);

        if (tipo == TipoMovimientoCaja.INGRESO) {
            caja.setSaldoActual(caja.getSaldoActual() + monto);
        } else {
            caja.setSaldoActual(caja.getSaldoActual() - monto);
        }
        cajaRepository.save(caja);

        return mov;
    }
}
