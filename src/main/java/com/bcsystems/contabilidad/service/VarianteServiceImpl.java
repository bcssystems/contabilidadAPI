package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.*;
import com.bcsystems.contabilidad.domain.en.EstadoProducto;
import com.bcsystems.contabilidad.domain.en.StatusProducto;
import com.bcsystems.contabilidad.domain.en.TipoMovimiento;
import com.bcsystems.contabilidad.dto.*;
import com.bcsystems.contabilidad.exception.InvalidEntryException;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.exception.RepeatedException;
import com.bcsystems.contabilidad.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class VarianteServiceImpl implements VarianteService {

    private final VarianteRepository varianteRepository;
    private final ProductoPadreRepository productoPadreRepository;
    private final ProveedorRepository proveedorRepository;
    private final UsuarioRepository usuarioRepository;
    private final SucursalRepository sucursalRepository;
    private final CostoPromedioService costoPromedioService;
    private final InventarioSucursalRepository inventarioSucursalRepository;
    private final VarianteAtributoRepository varianteAtributoRepository;
    private final AtributoValorRepository atributoValorRepository;
    private final MovimientoStockRepository movimientoStockRepository;
    private final ObjectMapper objectMapper;
    private final EntityManager entityManager;

    @Value("${app.upload.dir:uploads/productos}")
    private String uploadDir;

    public VarianteServiceImpl(VarianteRepository varianteRepository,
                                ProductoPadreRepository productoPadreRepository,
                                ProveedorRepository proveedorRepository,
                                UsuarioRepository usuarioRepository,
                                SucursalRepository sucursalRepository,
                                CostoPromedioService costoPromedioService,
                                InventarioSucursalRepository inventarioSucursalRepository,
                                VarianteAtributoRepository varianteAtributoRepository,
                                AtributoValorRepository atributoValorRepository,
                                MovimientoStockRepository movimientoStockRepository,
                                ObjectMapper objectMapper,
                                EntityManager entityManager) {
        this.varianteRepository = varianteRepository;
        this.productoPadreRepository = productoPadreRepository;
        this.proveedorRepository = proveedorRepository;
        this.usuarioRepository = usuarioRepository;
        this.sucursalRepository = sucursalRepository;
        this.costoPromedioService = costoPromedioService;
        this.inventarioSucursalRepository = inventarioSucursalRepository;
        this.varianteAtributoRepository = varianteAtributoRepository;
        this.atributoValorRepository = atributoValorRepository;
        this.movimientoStockRepository = movimientoStockRepository;
        this.objectMapper = objectMapper;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public Variante save(VarianteRegisterRequest request, List<MultipartFile> imagenesFiles) {
        if (varianteRepository.existsByNumeroSerie(request.numeroSerie())) {
            throw new RepeatedException("Ya existe una variante con el numero de serie: " + request.numeroSerie());
        }

        ProductoPadre padre = productoPadreRepository.findById(request.productoPadreId())
                .orElseThrow(() -> new NotFoundException("Producto padre no encontrado"));

        List<String> rutasImagenes = saveImageFiles(imagenesFiles);

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
                .estado(request.estado() != null ? request.estado() : EstadoProducto.SEMI)
                .precioCompra(request.precioCompra())
                .precioPublico(request.precioPublico())
                .precioConsigna(request.precioConsigna())
                .precioMayoreo(request.precioMayoreo())
                .observacion(request.observacion())
                .responsable(responsable)
                .sucursal(sucursal)
                .pagadoCaja(request.pagadoCaja() != null && request.pagadoCaja())
                .imagenes(toJson(rutasImagenes))
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

        variante = varianteRepository.save(variante);
        recalcularCostoPromedio(padre.getId());
        return variante;
    }

    @Override
    public Page<VarianteResponse> findByProductoPadreId(Long padreId, PageRequest pageRequest) {
        Page<Variante> variantes = varianteRepository.findByProductoPadreIdAndActivoTrue(padreId, pageRequest);
        return variantes.map(this::toResponse);
    }

    @Override
    public VarianteResponse findResponseById(Long id) {
        return toResponse(findById(id));
    }

    @Override
    public Variante findById(Long id) {
        return varianteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Variante no encontrada"));
    }

    @Override
    @Transactional
    public Variante update(Long id, VarianteUpdateRequest request, List<MultipartFile> imagenesFiles) {
        Variante variante = findById(id);

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
        if (request.status() != null) variante.setStatus(request.status());
        if (request.estado() != null) variante.setEstado(request.estado());
        if (request.precioCompra() != null) variante.setPrecioCompra(request.precioCompra());
        if (request.precioPublico() != null) variante.setPrecioPublico(request.precioPublico());
        if (request.precioConsigna() != null) variante.setPrecioConsigna(request.precioConsigna());
        if (request.precioMayoreo() != null) variante.setPrecioMayoreo(request.precioMayoreo());
        if (request.valeFolioSalida() != null) variante.setValeFolioSalida(request.valeFolioSalida());
        if (request.folioVenta() != null) variante.setFolioVenta(request.folioVenta());
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
            varianteAtributoRepository.deleteByVarianteId(id);
            entityManager.flush();
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
                InventarioSucursal inv = inventarioSucursalRepository
                        .findByVarianteIdAndSucursalIdSucursal(id, invReq.idSucursal())
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

        if (request.imagenesEliminar() != null && !request.imagenesEliminar().isEmpty()) {
            List<String> imagenesExistentes = fromJson(variante.getImagenes());
            List<String> imagenesActualizadas = new ArrayList<>(imagenesExistentes);
            for (String img : request.imagenesEliminar()) {
                imagenesActualizadas.remove(img);
                deleteImageFile(img);
            }
            variante.setImagenes(toJson(imagenesActualizadas));
        }

        if (imagenesFiles != null && !imagenesFiles.isEmpty()) {
            List<String> rutasImagenes = saveImageFiles(imagenesFiles);
            List<String> imagenesExistentes = fromJson(variante.getImagenes());
            imagenesExistentes.addAll(rutasImagenes);
            variante.setImagenes(toJson(imagenesExistentes));
        }

        variante = varianteRepository.save(variante);
        recalcularCostoPromedio(variante.getProductoPadre().getId());
        return variante;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Variante variante = findById(id);
        variante.setActivo(false);
        varianteRepository.save(variante);
        recalcularCostoPromedio(variante.getProductoPadre().getId());
    }

    @Override
    public Page<VarianteResponse> findDisponibles(PageRequest pageRequest) {
        Page<Variante> variantes = varianteRepository.findByActivoTrueAndStatusOrderByFechaEntradaAsc(
                StatusProducto.DISPONIBLE, pageRequest);
        return variantes.map(this::toResponse);
    }

    @Override
    public List<Variante> findDisponiblesByPadreId(Long padreId) {
        return varianteRepository.findByProductoPadreIdAndActivoTrue(padreId);
    }

    @Override
    @Transactional
    public VarianteResponse registrarMovimientoStock(Long id, MovimientoStockRequest request) {
        Variante variante = findById(id);

        Sucursal sucursal = null;
        if (request.idSucursal() != null) {
            sucursal = sucursalRepository.findById(request.idSucursal())
                    .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));
        }

        int cantidad = request.cantidad();
        boolean tieneSucursal = sucursal != null;

        if (tieneSucursal) {
            InventarioSucursal inventario = inventarioSucursalRepository
                    .findByVarianteIdAndSucursalIdSucursal(id, request.idSucursal())
                    .orElse(InventarioSucursal.builder()
                            .variante(variante)
                            .sucursal(sucursal)
                            .stock(0)
                            .build());

            int stockAnteriorSuc = inventario.getStock();
            int nuevoStockSuc;

            switch (request.tipoMovimiento()) {
                case ENTRADA -> nuevoStockSuc = stockAnteriorSuc + cantidad;
                case SALIDA -> {
                    if (stockAnteriorSuc < cantidad) {
                        throw new InvalidEntryException("Stock insuficiente en sucursal. Actual: " + stockAnteriorSuc +
                                ", solicitado: " + cantidad);
                    }
                    nuevoStockSuc = stockAnteriorSuc - cantidad;
                }
                default -> nuevoStockSuc = stockAnteriorSuc;
            }

            inventario.setStock(nuevoStockSuc);
            inventarioSucursalRepository.save(inventario);

            MovimientoStock movimiento = MovimientoStock.builder()
                    .variante(variante)
                    .sucursal(sucursal)
                    .tipoMovimiento(request.tipoMovimiento())
                    .cantidad(cantidad)
                    .stockAnterior(stockAnteriorSuc)
                    .stockNuevo(nuevoStockSuc)
                    .referencia(request.referencia())
                    .usuario("SISTEMA")
                    .observacion(request.observacion())
                    .build();
            movimientoStockRepository.save(movimiento);
        }

        return toResponse(variante);
    }

    @Override
    @Transactional
    public VarianteResponse transferirStock(Long id, TransferenciaRequest request) {
        Variante variante = findById(id);
        Sucursal origen = sucursalRepository.findById(request.idSucursalOrigen())
                .orElseThrow(() -> new NotFoundException("Sucursal origen no encontrada"));
        Sucursal destino = sucursalRepository.findById(request.idSucursalDestino())
                .orElseThrow(() -> new NotFoundException("Sucursal destino no encontrada"));

        if (origen.getIdSucursal().equals(destino.getIdSucursal())) {
            throw new InvalidEntryException("La sucursal origen y destino deben ser diferentes");
        }

        int cantidad = request.cantidad();

        InventarioSucursal invOrigen = inventarioSucursalRepository
                .findByVarianteIdAndSucursalIdSucursal(id, request.idSucursalOrigen())
                .orElseThrow(() -> new InvalidEntryException("Variante sin inventario en sucursal origen"));

        if (invOrigen.getStock() < cantidad) {
            throw new InvalidEntryException("Stock insuficiente en sucursal origen. Actual: " + invOrigen.getStock() +
                    ", solicitado: " + cantidad);
        }

        InventarioSucursal invDestino = inventarioSucursalRepository
                .findByVarianteIdAndSucursalIdSucursal(id, request.idSucursalDestino())
                .orElse(InventarioSucursal.builder()
                        .variante(variante)
                        .sucursal(destino)
                        .stock(0)
                        .build());

        int stockOrigenAntes = invOrigen.getStock();
        int stockDestinoAntes = invDestino.getStock();

        invOrigen.setStock(stockOrigenAntes - cantidad);
        invDestino.setStock(stockDestinoAntes + cantidad);

        inventarioSucursalRepository.save(invOrigen);
        inventarioSucursalRepository.save(invDestino);

        MovimientoStock movOrigen = MovimientoStock.builder()
                .variante(variante)
                .sucursal(origen)
                .tipoMovimiento(TipoMovimiento.SALIDA)
                .cantidad(cantidad)
                .stockAnterior(stockOrigenAntes)
                .stockNuevo(invOrigen.getStock())
                .referencia(request.referencia())
                .usuario("SISTEMA")
                .observacion(request.observacion() != null ? request.observacion() : "Transferido a " + destino.getNombre())
                .build();
        movimientoStockRepository.save(movOrigen);

        MovimientoStock movDestino = MovimientoStock.builder()
                .variante(variante)
                .sucursal(destino)
                .tipoMovimiento(TipoMovimiento.ENTRADA)
                .cantidad(cantidad)
                .stockAnterior(stockDestinoAntes)
                .stockNuevo(invDestino.getStock())
                .referencia(request.referencia())
                .usuario("SISTEMA")
                .observacion(request.observacion() != null ? request.observacion() : "Transferido desde " + origen.getNombre())
                .build();
        movimientoStockRepository.save(movDestino);

        return toResponse(variante);
    }

    @Override
    @Transactional
    public VarianteResponse actualizarStockSucursal(Long id, Integer idSucursal, Integer nuevoStock) {
        Variante variante = findById(id);
        Sucursal sucursal = sucursalRepository.findById(idSucursal)
                .orElseThrow(() -> new NotFoundException("Sucursal no encontrada"));

        InventarioSucursal inventario = inventarioSucursalRepository
                .findByVarianteIdAndSucursalIdSucursal(id, idSucursal)
                .orElse(InventarioSucursal.builder()
                        .variante(variante)
                        .sucursal(sucursal)
                        .stock(0)
                        .build());

        int diferencia = nuevoStock - inventario.getStock();
        inventario.setStock(nuevoStock);
        inventarioSucursalRepository.save(inventario);

        MovimientoStock movimiento = MovimientoStock.builder()
                .variante(variante)
                .sucursal(sucursal)
                .tipoMovimiento(diferencia >= 0 ? TipoMovimiento.ENTRADA : TipoMovimiento.SALIDA)
                .cantidad(Math.abs(diferencia))
                .stockAnterior(inventario.getStock() - diferencia)
                .stockNuevo(nuevoStock)
                .referencia("Ajuste manual")
                .usuario("SISTEMA")
                .observacion("Ajuste de stock en sucursal: " + sucursal.getNombre())
                .build();
        movimientoStockRepository.save(movimiento);

        return toResponse(variante);
    }

    private void recalcularCostoPromedio(Long productoPadreId) {
        costoPromedioService.recalcular(productoPadreId);
    }

    private VarianteResponse toResponse(Variante v) {
        String proveedorNombre = v.getProveedor() != null ? v.getProveedor().getNombre() : null;
        Long proveedorId = v.getProveedor() != null ? v.getProveedor().getIdProveedor() : null;

        String responsableNombre = null;
        Integer responsableId = null;
        if (v.getResponsable() != null) {
            responsableId = v.getResponsable().getIdUser();
            responsableNombre = v.getResponsable().getPersona() != null
                    ? v.getResponsable().getPersona().getNombre() + " " + v.getResponsable().getPersona().getPrimerApellido()
                    : v.getResponsable().getUsername();
        }

        Integer sucursalId = v.getSucursal() != null ? v.getSucursal().getIdSucursal() : null;
        String sucursalNombre = v.getSucursal() != null ? v.getSucursal().getNombre() : null;

        List<InventarioSucursalResponse> inventarios = v.getInventarioSucursales().stream()
                .map(i -> new InventarioSucursalResponse(
                        i.getId(),
                        i.getSucursal().getIdSucursal(),
                        i.getSucursal().getNombre(),
                        i.getStock(),
                        i.getStockMinimo(),
                        i.getStockMaximo()))
                .toList();

        List<AtributoAsignadoResponse> atributos = v.getVarianteAtributos().stream()
                .map(va -> new AtributoAsignadoResponse(
                        va.getAtributo().getIdAtributo(),
                        va.getAtributo().getNombre(),
                        va.getValor().getIdValor(),
                        va.getValor().getValor(),
                        va.getValor().getCodigoSku()))
                .toList();

        return new VarianteResponse(
                v.getId(),
                v.getProductoPadre().getId(),
                v.getProductoPadre().getNombre(),
                v.getProductoPadre().getSku(),
                v.getNombre(),
                v.getNumeroSerie(),
                v.getFechaEntrada(),
                v.getPorcentajeBateria(),
                proveedorId,
                proveedorNombre,
                v.getTipoCompra(),
                v.getUbicacion(),
                v.getImei(),
                v.getStatus(),
                v.getEstado(),
                v.getPrecioCompra(),
                v.getPrecioPublico(),
                v.getPrecioConsigna(),
                v.getPrecioMayoreo(),
                v.getValeFolioSalida(),
                v.getFolioVenta(),
                v.getObservacion(),
                responsableId,
                responsableNombre,
                sucursalId,
                sucursalNombre,
                fromJson(v.getImagenes()),
                v.getPagadoCaja(),
                v.getActivo(),
                v.getFechaCreacion(),
                inventarios,
                atributos
        );
    }

    private List<String> saveImageFiles(List<MultipartFile> files) {
        List<String> rutas = new ArrayList<>();
        if (files == null || files.isEmpty()) return rutas;

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;
                String originalFilename = file.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String uniqueFilename = UUID.randomUUID().toString() + extension;
                Path filePath = uploadPath.resolve(uniqueFilename);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                rutas.add(uniqueFilename);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar imagenes", e);
        }

        return rutas;
    }

    private void deleteImageFile(String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.out.println("No se pudo eliminar el archivo: " + filename + " - " + e.getMessage());
        }
    }

    private String toJson(List<String> list) {
        try {
            return list == null ? "[]" : objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> fromJson(String json) {
        try {
            if (json == null || json.isBlank()) return new ArrayList<>();
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
