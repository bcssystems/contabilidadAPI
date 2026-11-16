package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.MakerFilamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MakerFilamentoRepository extends JpaRepository<MakerFilamento, Integer> {
    List<MakerFilamento> findAllByOrderByNombreAsc();
    List<MakerFilamento> findByActivoTrueOrderByNombreAsc();
}
