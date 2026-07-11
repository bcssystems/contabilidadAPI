package com.bcsystems.contabilidad.repository;

import com.bcsystems.contabilidad.domain.VarianteAtributo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VarianteAtributoRepository extends JpaRepository<VarianteAtributo, Long> {

    List<VarianteAtributo> findByVarianteId(Long varianteId);

    void deleteByVarianteId(Long varianteId);
}
