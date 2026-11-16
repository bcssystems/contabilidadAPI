package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.MakerMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MakerMaterialRepository extends JpaRepository<MakerMaterial, Integer> {
    List<MakerMaterial> findAllByOrderByNombreAsc();
    List<MakerMaterial> findByActivoTrueOrderByNombreAsc();
    boolean existsByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Integer id);
}
