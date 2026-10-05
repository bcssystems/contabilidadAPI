package com.bcsystems.contabilidad.repository;
import com.bcsystems.contabilidad.domain.Marcaje;
import com.bcsystems.contabilidad.domain.en.TipoMarcaje;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MarcajeRepository extends JpaRepository<Marcaje, Integer> { boolean existsByAsistenciaIdAndTipo(Integer asistenciaId, TipoMarcaje tipo); }
