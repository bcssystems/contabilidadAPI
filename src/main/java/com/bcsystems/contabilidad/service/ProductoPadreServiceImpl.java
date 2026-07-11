package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.*;
import com.bcsystems.contabilidad.domain.en.EstadoProducto;
import com.bcsystems.contabilidad.domain.en.StatusProducto;
import com.bcsystems.contabilidad.dto.ProductoPadreRegisterRequest;
import com.bcsystems.contabilidad.dto.ProductoPadreResponse;
import com.bcsystems.contabilidad.dto.ProductoPadreUpdateRequest;
import com.bcsystems.contabilidad.dto.InventarioSucursalRequest;
import com.bcsystems.contabilidad.dto.VarianteRegisterRequest;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.exception.RepeatedException;
import com.bcsystems.contabilidad.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductoPadreServiceImpl implements ProductoPadreService {

    private final ProductoPadreRepository productoPadreRepository;
    private final VarianteRepository varianteRepository;
    private final ProveedorRepository proveedorRepository;
    private final UsuarioRepository usuarioRepository;
    private final SucursalRepository sucursalRepository;
    private final CostoPromedioRepository costoPromedioRepository;
    private final CostoPromedioService costoPromedioService;
    private final AtributoValorRepository atributoValorRepository;
    private final InventarioSucursalRepository inventarioSucursalRepository;
    private final VarianteAtributoRepository varianteAtributoRepository;
    private final ObjectMapper objectMapper;
    private final jakarta.persistence.EntityManager entityManager;

    public ProductoPadreServiceImpl(ProductoPadreRepository productoPadreRepository,
                                    VarianteRepository varianteRepository,
                                    ProveedorRepository proveedorRepository,
                                    UsuarioRepository usuarioRepository,
                                    SucursalRepository sucursalRepository,
                                    CostoPromedioRepository costoPromedioRepository,
                                    CostoPromedioService costoPromedioService,
                                    AtributoValorRepository atributoValorRepository,
                                    InventarioSucursalRepository inventarioSucursalRepository,
                                    VarianteAtributoRepository varianteAtributoRepository,
                                    ObjectMapper objectMapper,
                                    jakarta.persistence.EntityManager entityManager) {
        this.productoPadreRepository = productoPadreRepository;
        this.varianteRepository = varianteRepository;
        this.proveedorRepository = proveedorRepository;
        this.usuarioRepository = usuarioRepository;
        this.sucursalRepository = sucursalRepository;
        this.costoPromedioRepository = costoPromedioRepository;
        this.costoPromedioService = costoPromedioService;
        this.atributoValorRepository = atributoValorRepository;
        this.inventarioSucursalRepository = inventarioSucursalRepository;
        this.varianteAtributoRepository = varianteAtributoRepository;
        this.objectMapper = objectMapper;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public ProductoPadreResponse save(ProductoPadreRegisterRequest request) {
        if (productoPadreRepository.existsBySku(request.sku())) {
            throw new RepeatedException("Ya existe un producto con el SKU: " + request.sku());
        }

        boolean tieneVariantes = request.tieneVariantes() != null && request.tieneVariantes();

        ProductoPadre padre = ProductoPadre.builder()
                .sku(request.sku())
                .nombre(request.nombre())
                .marca(request.marca())
                .modelo(request.modelo())
                .categoria(request.categoria())
                .descripcion(request.descripcion())
                .mensajeWhatsapp(request.mensajeWhatsapp() != null ? request.mensajeWhatsapp() :
                        "Hola, me interesa este producto. Podrian darme mas informacion?")
                .tieneVariantes(tieneVariantes)
                .build();

        padre = productoPadreRepository.save(padre);
        return toResponse(padre);
    }

    @Override
    @Transactional
    public ProductoPadreResponse saveWithVariantes(ProductoPadreRegisterRequest request) {
        if (productoPadreRepository.existsBySku(request.sku())) {
            throw new RepeatedException("Ya existe un producto con el SKU: " + request.sku());
        }

        ProductoPadre padre = ProductoPadre.builder()
                .sku(request.sku())
                .nombre(request.nombre())
                .marca(request.marca())
                .modelo(request.modelo())
                .categoria(request.categoria())
                .descripcion(request.descripcion())
                .mensajeWhatsapp(request.mensajeWhatsapp() != null ? request.mensajeWhatsapp() :
                        "Hola, me interesa este producto. Podrian darme mas informacion?")
                .tieneVariantes(true)
                .build();

        padre = productoPadreRepository.save(padre);

        if (request.variantes() != null) {
            for (VarianteRegisterRequest vr : request.variantes()) {
                if (varianteRepository.existsByNumeroSerie(vr.numeroSerie())) {
                    throw new RepeatedException("Ya existe una variante con el numero de serie: " + vr.numeroSerie());
                }

                Proveedor proveedor = null;
                if (vr.proveedorId() != null) {
                    proveedor = proveedorRepository.findById(vr.proveedorId())
                            .orElseThrow(() -> new NotFoundException("Proveedor no encontrado"));
                }

                Usuario responsable = null;
                if (vr.responsableId() != null) {
                    responsable = usuarioRepository.findById(vr.responsableId())
                            .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
                }

                Sucursal sucursal = null;
                if (vr.sucursalId() != null) {
                    sucursal = sucursalRepository.findById(vr.sucursalId())
                            .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));
                }

                Variante variante = Variante.builder()
                        .productoPadre(padre)
                        .nombre(vr.nombre())
                        .numeroSerie(vr.numeroSerie())
                        .fechaEntrada(vr.fechaEntrada())
                        .porcentajeBateria(vr.porcentajeBateria())
                        .proveedor(proveedor)
                        .tipoCompra(vr.tipoCompra())
                        .ubicacion(vr.ubicacion())
                        .imei(vr.imei())
                        .estado(vr.estado() != null ? vr.estado() : EstadoProducto.SEMI)
                        .precioCompra(vr.precioCompra())
                        .precioPublico(vr.precioPublico())
                        .precioConsigna(vr.precioConsigna())
                        .precioMayoreo(vr.precioMayoreo())
                        .observacion(vr.observacion())
                        .responsable(responsable)
                        .sucursal(sucursal)
                        .pagadoCaja(vr.pagadoCaja() != null && vr.pagadoCaja())
                        .build();

                if (vr.idAtributoValores() != null) {
                    List<AtributoValor> valores = atributoValorRepository.findByIdValorIn(vr.idAtributoValores());
                    for (AtributoValor valor : valores) {
                        VarianteAtributo va = VarianteAtributo.builder()
                                .variante(variante)
                                .atributo(valor.getAtributo())
                                .valor(valor)
                                .build();
                        variante.getVarianteAtributos().add(va);
                    }
                }

                if (vr.inventarios() != null) {
                    for (InventarioSucursalRequest invReq : vr.inventarios()) {
                        Sucursal suc = sucursalRepository.findById(invReq.idSucursal())
                                .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));
                        InventarioSucursal inv = InventarioSucursal.builder()
                                .variante(variante)
                                .sucursal(suc)
                                .stock(invReq.stock() != null ? invReq.stock() : 0)
                                .stockMinimo(invReq.stockMinimo())
                                .stockMaximo(invReq.stockMaximo())
                                .build();
                        variante.getInventarioSucursales().add(inv);
                    }
                }

                varianteRepository.save(variante);
            }

            costoPromedioService.recalcular(padre.getId());
        }

        return toResponse(padre);
    }

    @Override
    public Page<ProductoPadreResponse> findAllPageable(PageRequest pageRequest) {
        Page<ProductoPadre> padres = productoPadreRepository.findByActivoTrue(pageRequest);
        return padres.map(this::toResponse);
    }

    @Override
    public ProductoPadre findById(Long id) {
        return productoPadreRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Producto padre no encontrado"));
    }

    @Override
    public ProductoPadreResponse findResponseById(Long id) {
        return toResponse(findById(id));
    }

    @Override
    @Transactional
    public ProductoPadreResponse update(Long id, ProductoPadreUpdateRequest request) {
        ProductoPadre padre = findById(id);

        if (request.sku() != null && !request.sku().equals(padre.getSku())) {
            if (productoPadreRepository.existsBySku(request.sku())) {
                throw new RepeatedException("Ya existe un producto con el SKU: " + request.sku());
            }
            padre.setSku(request.sku());
        }
        if (request.nombre() != null) padre.setNombre(request.nombre());
        if (request.marca() != null) padre.setMarca(request.marca());
        if (request.modelo() != null) padre.setModelo(request.modelo());
        if (request.categoria() != null) padre.setCategoria(request.categoria());
        if (request.descripcion() != null) padre.setDescripcion(request.descripcion());
        if (request.mensajeWhatsapp() != null) padre.setMensajeWhatsapp(request.mensajeWhatsapp());
        if (request.tieneVariantes() != null) padre.setTieneVariantes(request.tieneVariantes());
        if (request.activo() != null) padre.setActivo(request.activo());

        padre = productoPadreRepository.save(padre);

        if (request.variantes() != null) {
            syncVariantes(padre, request.variantes());
            costoPromedioService.recalcular(padre.getId());
        }

        return toResponse(padre);
    }

    @Transactional
    protected void syncVariantes(ProductoPadre padre, List<com.bcsystems.contabilidad.dto.VarianteUpdateRequest> variantesRequest) {
        List<Variante> existingVariantes = varianteRepository.findByProductoPadreIdAndActivoTrue(padre.getId());
        java.util.Set<Long> incomingIds = new java.util.HashSet<>();

        for (com.bcsystems.contabilidad.dto.VarianteUpdateRequest vr : variantesRequest) {
            if (vr.id() != null) {
                incomingIds.add(vr.id());
                Variante variante = existingVariantes.stream()
                        .filter(v -> v.getId().equals(vr.id()))
                        .findFirst()
                        .orElseThrow(() -> new NotFoundException("Variante no encontrada: " + vr.id()));

                updateVarianteFields(variante, vr);
            } else {
                createVarianteFromRequest(padre, vr);
            }
        }

        for (Variante v : existingVariantes) {
            if (!incomingIds.contains(v.getId())) {
                v.setActivo(false);
                varianteRepository.save(v);
            }
        }
    }

    private void updateVarianteFields(Variante variante, com.bcsystems.contabilidad.dto.VarianteUpdateRequest request) {
        if (request.nombre() != null) variante.setNombre(request.nombre());
        if (request.numeroSerie() != null) {
            if (!request.numeroSerie().equals(variante.getNumeroSerie()) &&
                    varianteRepository.existsByNumeroSerie(request.numeroSerie())) {
                throw new RepeatedException("Ya existe una variante con el numero de serie: " + request.numeroSerie());
            }
            variante.setNumeroSerie(request.numeroSerie());
        }
        if (request.fechaEntrada() != null) variante.setFechaEntrada(request.fechaEntrada());
        if (request.porcentajeBateria() != null) variante.setPorcentajeBateria(request.porcentajeBateria());
        if (request.proveedorId() != null) {
            Proveedor proveedor = proveedorRepository.findById(request.proveedorId())
                    .orElseThrow(() -> new NotFoundException("Proveedor no encontrado"));
            variante.setProveedor(proveedor);
        }
        if (request.tipoCompra() != null) variante.setTipoCompra(request.tipoCompra());
        if (request.ubicacion() != null) variante.setUbicacion(request.ubicacion());
        if (request.imei() != null) variante.setImei(request.imei());
        if (request.estado() != null) variante.setEstado(request.estado());
        if (request.precioCompra() != null) variante.setPrecioCompra(request.precioCompra());
        if (request.precioPublico() != null) variante.setPrecioPublico(request.precioPublico());
        if (request.precioConsigna() != null) variante.setPrecioConsigna(request.precioConsigna());
        if (request.precioMayoreo() != null) variante.setPrecioMayoreo(request.precioMayoreo());
        if (request.observacion() != null) variante.setObservacion(request.observacion());
        if (request.responsableId() != null) {
            Usuario responsable = usuarioRepository.findById(request.responsableId())
                    .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
            variante.setResponsable(responsable);
        }
        if (request.sucursalId() != null) {
            Sucursal sucursal = sucursalRepository.findById(request.sucursalId())
                    .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));
            variante.setSucursal(sucursal);
        }
        if (request.pagadoCaja() != null) variante.setPagadoCaja(request.pagadoCaja());
        if (request.activo() != null) variante.setActivo(request.activo());

        if (request.idAtributoValores() != null) {
            varianteAtributoRepository.deleteByVarianteId(variante.getId());
            entityManager.flush();
            List<AtributoValor> valores = atributoValorRepository.findByIdValorIn(request.idAtributoValores());
            variante.getVarianteAtributos().clear();
            for (AtributoValor valor : valores) {
                VarianteAtributo va = VarianteAtributo.builder()
                        .variante(variante)
                        .atributo(valor.getAtributo())
                        .valor(valor)
                        .build();
                variante.getVarianteAtributos().add(va);
            }
        }

        if (request.inventarios() != null) {
            for (InventarioSucursalRequest invReq : request.inventarios()) {
                Sucursal suc = sucursalRepository.findById(invReq.idSucursal())
                        .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));
                InventarioSucursal inv = inventarioSucursalRepository
                        .findByVarianteIdAndSucursalIdSucursal(variante.getId(), invReq.idSucursal())
                        .orElse(InventarioSucursal.builder()
                                .variante(variante)
                                .sucursal(suc)
                                .stock(0)
                                .build());
                inv.setStock(invReq.stock() != null ? invReq.stock() : 0);
                inv.setStockMinimo(invReq.stockMinimo());
                inv.setStockMaximo(invReq.stockMaximo());
                inventarioSucursalRepository.save(inv);
            }
        }

        varianteRepository.save(variante);
    }

    private void createVarianteFromRequest(ProductoPadre padre, com.bcsystems.contabilidad.dto.VarianteUpdateRequest request) {
        if (request.numeroSerie() != null && varianteRepository.existsByNumeroSerie(request.numeroSerie())) {
            throw new RepeatedException("Ya existe una variante con el numero de serie: " + request.numeroSerie());
        }

        Proveedor proveedor = null;
        if (request.proveedorId() != null) {
            proveedor = proveedorRepository.findById(request.proveedorId())
                    .orElseThrow(() -> new NotFoundException("Proveedor no encontrado"));
        }

        Usuario responsable = null;
        if (request.responsableId() != null) {
            responsable = usuarioRepository.findById(request.responsableId())
                    .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        }

        Sucursal sucursal = null;
        if (request.sucursalId() != null) {
            sucursal = sucursalRepository.findById(request.sucursalId())
                    .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));
        }

        Variante variante = Variante.builder()
                .productoPadre(padre)
                .nombre(request.nombre())
                .numeroSerie(request.numeroSerie())
                .fechaEntrada(request.fechaEntrada())
                .porcentajeBateria(request.porcentajeBateria())
                .proveedor(proveedor)
                .tipoCompra(request.tipoCompra())
                .ubicacion(request.ubicacion())
                .imei(request.imei())
                .estado(request.estado() != null ? request.estado() : com.bcsystems.contabilidad.domain.en.EstadoProducto.SEMI)
                .precioCompra(request.precioCompra())
                .precioPublico(request.precioPublico())
                .precioConsigna(request.precioConsigna())
                .precioMayoreo(request.precioMayoreo())
                .observacion(request.observacion())
                .responsable(responsable)
                .sucursal(sucursal)
                .pagadoCaja(request.pagadoCaja() != null && request.pagadoCaja())
                .build();

        if (request.idAtributoValores() != null) {
            List<AtributoValor> valores = atributoValorRepository.findByIdValorIn(request.idAtributoValores());
            for (AtributoValor valor : valores) {
                VarianteAtributo va = VarianteAtributo.builder()
                        .variante(variante)
                        .atributo(valor.getAtributo())
                        .valor(valor)
                        .build();
                variante.getVarianteAtributos().add(va);
            }
        }

        if (request.inventarios() != null) {
            for (InventarioSucursalRequest invReq : request.inventarios()) {
                Sucursal suc = sucursalRepository.findById(invReq.idSucursal())
                        .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));
                InventarioSucursal inv = InventarioSucursal.builder()
                        .variante(variante)
                        .sucursal(suc)
                        .stock(invReq.stock() != null ? invReq.stock() : 0)
                        .stockMinimo(invReq.stockMinimo())
                        .stockMaximo(invReq.stockMaximo())
                        .build();
                variante.getInventarioSucursales().add(inv);
            }
        }

        varianteRepository.save(variante);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ProductoPadre padre = findById(id);
        padre.setActivo(false);
        productoPadreRepository.save(padre);
    }

    private ProductoPadreResponse toResponse(ProductoPadre p) {
        long totalVariantes = varianteRepository.countByProductoPadreIdAndActivoTrueAndStatus(
                p.getId(), StatusProducto.DISPONIBLE);
        long variantesDisponibles = totalVariantes;

        String costoPromedioStr = "0.00";
        Optional<CostoPromedio> cp = costoPromedioRepository.findByProductoPadreId(p.getId());
        if (cp.isPresent()) {
            costoPromedioStr = cp.get().getCostoPromedio().setScale(2, RoundingMode.HALF_UP).toString();
        }

        return new ProductoPadreResponse(
                p.getId(),
                p.getSku(),
                p.getNombre(),
                p.getMarca(),
                p.getModelo(),
                p.getCategoria(),
                p.getDescripcion(),
                p.getMensajeWhatsapp(),
                p.getTieneVariantes(),
                p.getActivo(),
                p.getFechaCreacion(),
                varianteRepository.countByProductoPadreIdAndActivoTrueAndStatus(p.getId(), null),
                variantesDisponibles,
                costoPromedioStr
        );
    }

    private String toJson(List<String> list) {
        try {
            return list == null ? "[]" : objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }
}
