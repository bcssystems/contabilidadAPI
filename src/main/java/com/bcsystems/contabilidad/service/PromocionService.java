package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Promocion;
import com.bcsystems.contabilidad.domain.en.TipoPromocion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PromocionService {
    Page<Promocion> buscarConFiltros(TipoPromocion tipo, Boolean activo, Pageable pageable);
    Promocion findById(Integer id);
    Promocion save(Promocion promocion);
    Promocion update(Integer id, Promocion promocion);
    void delete(Integer id);
}
