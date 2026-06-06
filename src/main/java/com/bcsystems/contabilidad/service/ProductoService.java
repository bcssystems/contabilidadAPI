package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Producto;
import com.bcsystems.contabilidad.domain.en.CategoriaProducto;
import com.bcsystems.contabilidad.domain.en.EstadoProducto;
import com.bcsystems.contabilidad.dto.ProductoAdminResponse;
import com.bcsystems.contabilidad.dto.ProductoEcommerceResponse;
import com.bcsystems.contabilidad.dto.ProductoRegisterRequest;
import com.bcsystems.contabilidad.dto.ProductoUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductoService {

    Producto save(ProductoRegisterRequest request, List<MultipartFile> imagenesFiles);

    Page<ProductoAdminResponse> findAllPageable(PageRequest pageRequest);

    Producto findById(Long id);

    ProductoAdminResponse findAdminById(Long id);

    Producto update(Long id, ProductoUpdateRequest request, List<MultipartFile> imagenesFiles);

    void delete(Long id);

    Page<ProductoEcommerceResponse> findAllEcommercePageable(PageRequest pageRequest);

    ProductoEcommerceResponse findEcommerceById(Long id);

    Page<ProductoEcommerceResponse> findByCategoriaEcommerce(PageRequest pageRequest, CategoriaProducto categoria);

    Page<ProductoEcommerceResponse> findByEstadoEcommerce(PageRequest pageRequest, EstadoProducto estado);

    Page<ProductoEcommerceResponse> findByCategoriaAndEstadoEcommerce(PageRequest pageRequest, CategoriaProducto categoria, EstadoProducto estado);
}
