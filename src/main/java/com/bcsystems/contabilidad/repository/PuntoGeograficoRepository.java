package com.bcsystems.contabilidad.repository;
import com.bcsystems.contabilidad.domain.PuntoGeografico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface PuntoGeograficoRepository extends JpaRepository<PuntoGeografico, Integer> { List<PuntoGeografico> findByActivoTrue(); }
