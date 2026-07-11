package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.Gasto;
import com.bcsystems.contabilidad.domain.en.EstadoGasto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GastoRepository extends JpaRepository<Gasto, Integer> {
    List<Gasto> findByEstado(EstadoGasto estado);
    List<Gasto> findByCajaIdCaja(Integer idCaja);
    List<Gasto> findByCajaIdCajaAndEstado(Integer idCaja, EstadoGasto estado);
}
