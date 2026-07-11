package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.Credito;
import com.bcsystems.contabilidad.domain.en.EstadoCredito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditoRepository extends JpaRepository<Credito, Integer> {
    List<Credito> findByClienteIdClienteOrderByFechaCreacionDesc(Integer idCliente);
    List<Credito> findByEstado(EstadoCredito estado);
    List<Credito> findByClienteIdClienteAndEstado(Integer idCliente, EstadoCredito estado);
}
