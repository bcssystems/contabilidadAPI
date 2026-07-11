package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.ProductoPadre;
import com.bcsystems.contabilidad.dto.ProductoPadreRegisterRequest;
import com.bcsystems.contabilidad.dto.ProductoPadreResponse;
import com.bcsystems.contabilidad.dto.ProductoPadreUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ProductoPadreService {

    ProductoPadreResponse save(ProductoPadreRegisterRequest request);

    ProductoPadreResponse saveWithVariantes(ProductoPadreRegisterRequest request);

    Page<ProductoPadreResponse> findAllPageable(PageRequest pageRequest);

    ProductoPadre findById(Long id);

    ProductoPadreResponse findResponseById(Long id);

    ProductoPadreResponse update(Long id, ProductoPadreUpdateRequest request);

    void delete(Long id);
}
