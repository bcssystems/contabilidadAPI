package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.CostoPromedio;
import com.bcsystems.contabilidad.domain.ProductoPadre;
import com.bcsystems.contabilidad.domain.Variante;
import com.bcsystems.contabilidad.domain.en.StatusProducto;
import com.bcsystems.contabilidad.dto.CostoPromedioResponse;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.CostoPromedioRepository;
import com.bcsystems.contabilidad.repository.ProductoPadreRepository;
import com.bcsystems.contabilidad.repository.VarianteRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CostoPromedioServiceImpl implements CostoPromedioService {

    private final CostoPromedioRepository costoPromedioRepository;
    private final ProductoPadreRepository productoPadreRepository;
    private final VarianteRepository varianteRepository;

    public CostoPromedioServiceImpl(CostoPromedioRepository costoPromedioRepository,
                                    ProductoPadreRepository productoPadreRepository,
                                    VarianteRepository varianteRepository) {
        this.costoPromedioRepository = costoPromedioRepository;
        this.productoPadreRepository = productoPadreRepository;
        this.varianteRepository = varianteRepository;
    }

    @Override
    @Transactional
    public CostoPromedioResponse getCostoPromedioByProductoPadreId(Long productoPadreId) {
        CostoPromedio cp = costoPromedioRepository.findByProductoPadreId(productoPadreId)
                .orElseGet(() -> recalcularInterno(productoPadreId));
        return toResponse(cp);
    }

    @Override
    @Transactional
    public CostoPromedioResponse recalcular(Long productoPadreId) {
        CostoPromedio cp = recalcularInterno(productoPadreId);
        return toResponse(cp);
    }

    private CostoPromedio recalcularInterno(Long productoPadreId) {
        ProductoPadre padre = productoPadreRepository.findById(productoPadreId)
                .orElseThrow(() -> new NotFoundException("Producto padre no encontrado"));

        List<Variante> disponibles = varianteRepository
                .findByProductoPadreIdAndActivoTrue(productoPadreId);

        BigDecimal sumaCostos = BigDecimal.ZERO;
        long cantidad = 0;

        for (Variante v : disponibles) {
            if (v.getPrecioCompra() != null && v.getStatus() == StatusProducto.DISPONIBLE) {
                sumaCostos = sumaCostos.add(v.getPrecioCompra());
                cantidad++;
            }
        }

        BigDecimal promedio = BigDecimal.ZERO;
        if (cantidad > 0) {
            promedio = sumaCostos.divide(BigDecimal.valueOf(cantidad), 2, RoundingMode.HALF_UP);
        }

        CostoPromedio cp = costoPromedioRepository.findByProductoPadreId(productoPadreId)
                .orElse(CostoPromedio.builder()
                        .productoPadre(padre)
                        .build());

        cp.setCostoPromedio(promedio);
        cp.setCantidadTotal((int) cantidad);
        cp.setUltimaActualizacion(LocalDateTime.now());

        return costoPromedioRepository.save(cp);
    }

    private CostoPromedioResponse toResponse(CostoPromedio cp) {
        return new CostoPromedioResponse(
                cp.getProductoPadre().getId(),
                cp.getProductoPadre().getSku(),
                cp.getProductoPadre().getNombre(),
                cp.getCostoPromedio(),
                cp.getCantidadTotal(),
                cp.getUltimaActualizacion()
        );
    }
}
