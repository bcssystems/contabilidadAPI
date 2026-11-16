package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.MakerConfiguracion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MakerConfiguracionRepository extends JpaRepository<MakerConfiguracion, Integer> {
    Optional<MakerConfiguracion> findByClave(String clave);
}
