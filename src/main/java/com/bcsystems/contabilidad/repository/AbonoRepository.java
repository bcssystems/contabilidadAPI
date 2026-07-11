package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.Abono;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AbonoRepository extends JpaRepository<Abono, Integer> {
    List<Abono> findByCreditoIdCreditoOrderByFechaDesc(Integer idCredito);
    List<Abono> findByCreditoClienteIdClienteOrderByFechaDesc(Integer idCliente);
}
