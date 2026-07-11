package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.Caja;
import com.bcsystems.contabilidad.domain.en.CajaEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CajaRepository extends JpaRepository<Caja, Integer> {
    List<Caja> findBySucursalIdSucursal(Integer idSucursal);
    List<Caja> findByActivaTrue();
    List<Caja> findBySucursalIdSucursalAndEstado(Integer idSucursal, CajaEstado estado);
    List<Caja> findBySucursalIdSucursalAndActivaTrue(Integer idSucursal);
}
