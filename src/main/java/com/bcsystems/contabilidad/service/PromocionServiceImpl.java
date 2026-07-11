package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Promocion;
import com.bcsystems.contabilidad.domain.en.TipoPromocion;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.PromocionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PromocionServiceImpl implements PromocionService {

    private final PromocionRepository promocionRepository;

    public PromocionServiceImpl(PromocionRepository promocionRepository) {
        this.promocionRepository = promocionRepository;
    }

    @Override
    public Page<Promocion> buscarConFiltros(TipoPromocion tipo, Boolean activo, Pageable pageable) {
        return promocionRepository.buscarConFiltros(tipo, activo, pageable);
    }

    @Override
    public Promocion findById(Integer id) {
        return promocionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Promocion no encontrada"));
    }

    @Override
    @Transactional
    public Promocion save(Promocion promocion) {
        for (com.bcsystems.contabilidad.domain.PromocionDetalle detalle : promocion.getDetalles()) {
            detalle.setPromocion(promocion);
        }
        return promocionRepository.save(promocion);
    }

    @Override
    @Transactional
    public Promocion update(Integer id, Promocion promocion) {
        Promocion existing = findById(id);
        if (promocion.getNombre() != null) existing.setNombre(promocion.getNombre());
        if (promocion.getDescripcion() != null) existing.setDescripcion(promocion.getDescripcion());
        if (promocion.getTipo() != null) existing.setTipo(promocion.getTipo());
        if (promocion.getDescuentoPorcentaje() != null) existing.setDescuentoPorcentaje(promocion.getDescuentoPorcentaje());
        if (promocion.getPrecioSugerido() != null) existing.setPrecioSugerido(promocion.getPrecioSugerido());
        if (promocion.getPrecioFinal() != null) existing.setPrecioFinal(promocion.getPrecioFinal());
        if (promocion.getIdProducto() != null) existing.setIdProducto(promocion.getIdProducto());
        if (promocion.getActivo() != null) existing.setActivo(promocion.getActivo());
        if (promocion.getFechaInicio() != null) existing.setFechaInicio(promocion.getFechaInicio());
        if (promocion.getFechaFin() != null) existing.setFechaFin(promocion.getFechaFin());

        if (promocion.getDetalles() != null && !promocion.getDetalles().isEmpty()) {
            existing.getDetalles().clear();
            for (com.bcsystems.contabilidad.domain.PromocionDetalle detalle : promocion.getDetalles()) {
                detalle.setPromocion(existing);
                existing.getDetalles().add(detalle);
            }
        }

        return promocionRepository.save(existing);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        Promocion promocion = findById(id);
        promocion.setActivo(false);
        promocionRepository.save(promocion);
    }
}
