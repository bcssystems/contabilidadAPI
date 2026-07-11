package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.*;
import com.bcsystems.contabilidad.domain.en.EstadoCredito;
import com.bcsystems.contabilidad.domain.en.TipoAbono;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CreditoServiceImpl implements CreditoService {

    private final CreditoRepository creditoRepository;
    private final AbonoRepository abonoRepository;
    private final ClienteRepository clienteRepository;
    private final PersonaRepository personaRepository;
    private final VentaRepository ventaRepository;

    public CreditoServiceImpl(CreditoRepository creditoRepository,
                              AbonoRepository abonoRepository,
                              ClienteRepository clienteRepository,
                              PersonaRepository personaRepository,
                              VentaRepository ventaRepository) {
        this.creditoRepository = creditoRepository;
        this.abonoRepository = abonoRepository;
        this.clienteRepository = clienteRepository;
        this.personaRepository = personaRepository;
        this.ventaRepository = ventaRepository;
    }

    @Override
    public List<Credito> findByCliente(Integer idCliente) {
        return creditoRepository.findByClienteIdClienteOrderByFechaCreacionDesc(idCliente);
    }

    @Override
    public Credito findById(Integer id) {
        return creditoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Credito no encontrado"));
    }

    @Override
    @Transactional
    public Credito save(Credito credito) {
        if (credito.getFechaCreacion() == null) {
            credito.setFechaCreacion(LocalDateTime.now());
        }
        if (credito.getEstado() == null) {
            credito.setEstado(EstadoCredito.ACTIVO);
        }
        return creditoRepository.save(credito);
    }

    @Override
    @Transactional
    public Abono registrarAbono(Integer idCredito, Double monto, String tipo, Integer idUsuario) {
        Credito credito = findById(idCredito);
        Persona usuario = personaRepository.findById(idUsuario)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        Abono abono = Abono.builder()
                .credito(credito)
                .monto(monto)
                .tipo("LIQUIDACION".equalsIgnoreCase(tipo) ? TipoAbono.LIQUIDACION : TipoAbono.PARCIAL)
                .fecha(LocalDateTime.now())
                .usuario(usuario)
                .build();
        abono = abonoRepository.save(abono);

        credito.setSaldoPendiente(credito.getSaldoPendiente() - monto);
        if (credito.getSaldoPendiente() <= 0) {
            credito.setEstado(EstadoCredito.PAGADO);
            credito.setSaldoPendiente(0.0);
        }
        creditoRepository.save(credito);

        Cliente cliente = credito.getCliente();
        cliente.setSaldoActual(cliente.getSaldoActual() - monto);
        if (cliente.getSaldoActual() < 0) cliente.setSaldoActual(0.0);
        clienteRepository.save(cliente);

        return abono;
    }

    @Override
    @Transactional
    public Abono registrarAbonoGeneral(Integer idCliente, Double monto, Integer idUsuario) {
        List<Credito> creditosActivos = creditoRepository
                .findByClienteIdClienteAndEstado(idCliente, EstadoCredito.ACTIVO);

        if (creditosActivos.isEmpty()) {
            throw new NotFoundException("El cliente no tiene creditos activos");
        }

        double restante = monto;
        Abono lastAbono = null;

        for (Credito credito : creditosActivos) {
            if (restante <= 0) break;
            double pago = Math.min(restante, credito.getSaldoPendiente());
            lastAbono = registrarAbono(credito.getIdCredito(), pago, "PARCIAL", idUsuario);
            restante -= pago;
        }

        return lastAbono;
    }

    @Override
    public List<Map<String, Object>> getMovimientosCliente(Integer idCliente) {
        List<Map<String, Object>> movimientos = new ArrayList<>();
        List<Credito> creditos = creditoRepository.findByClienteIdClienteOrderByFechaCreacionDesc(idCliente);

        for (Credito credito : creditos) {
            Map<String, Object> mov = new HashMap<>();
            mov.put("tipo", "CREDITO");
            mov.put("creditoId", credito.getIdCredito());
            mov.put("monto", credito.getMontoOriginal());
            mov.put("saldoPendiente", credito.getSaldoPendiente());
            mov.put("fecha", credito.getFechaCreacion());
            mov.put("estado", credito.getEstado());
            mov.put("ventaId", credito.getVenta() != null ? credito.getVenta().getIdVenta() : null);
            movimientos.add(mov);

            List<Abono> abonos = abonoRepository.findByCreditoIdCreditoOrderByFechaDesc(credito.getIdCredito());
            for (Abono abono : abonos) {
                Map<String, Object> ab = new HashMap<>();
                ab.put("tipo", "ABONO");
                ab.put("creditoId", credito.getIdCredito());
                ab.put("monto", abono.getMonto());
                ab.put("fecha", abono.getFecha());
                ab.put("tipoAbono", abono.getTipo());
                ab.put("abonoId", abono.getIdAbono());
                movimientos.add(ab);
            }
        }

        return movimientos;
    }
}
