package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.*;
import com.bcsystems.contabilidad.dto.*;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.exception.RepeatedException;
import com.bcsystems.contabilidad.repository.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class MakerService {
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;
    private final MakerMaterialRepository materialRepository;
    private final MakerFilamentoRepository filamentoRepository;
    private final MakerConfiguracionRepository configRepository;
    private final MakerCotizacionRepository cotizacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final MakerProyectoRepository proyectoRepository;

    public MakerService(MakerMaterialRepository materialRepository, MakerFilamentoRepository filamentoRepository,
                        MakerConfiguracionRepository configRepository, MakerCotizacionRepository cotizacionRepository,
                        UsuarioRepository usuarioRepository, MakerProyectoRepository proyectoRepository) {
        this.materialRepository = materialRepository;
        this.filamentoRepository = filamentoRepository;
        this.configRepository = configRepository;
        this.cotizacionRepository = cotizacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.proyectoRepository = proyectoRepository;
    }

    @Transactional
    public void ensureDefaults() {
        defaultConfig("COSTO_KWH", "3.50", "MXN/kWh", "Costo de electricidad");
        defaultConfig("CONSUMO_KW", "0.095", "kW", "Consumo promedio de Bambu Lab A1 durante impresión PLA (95 W)");
        defaultConfig("MERMA_MONOCOLOR", "0.03", "%", "Merma proporcional para impresión monocolor");
        defaultConfig("MERMA_MULTICOLOR", "0.15", "%", "Merma proporcional para impresión multicolor");
        defaultConfig("PURGA_POR_CAMBIO", "3.00", "g", "Gramos de purga por cambio de color");
        defaultConfig("MARGEN_PUBLICO", "0.50", "%", "Margen para público");
        defaultConfig("MARGEN_MAYOREO", "0.35", "%", "Margen para mayoreo");
        defaultConfig("MARGEN_DISTRIBUIDOR", "0.20", "%", "Margen para distribuidor");
    }

    private void defaultConfig(String key, String value, String unit, String description) {
        if (configRepository.findByClave(key).isEmpty()) {
            configRepository.save(MakerConfiguracion.builder().clave(key).valor(new BigDecimal(value))
                    .unidad(unit).descripcion(description).build());
        }
    }

    public List<MakerMaterial> materials(boolean all) {
        return all ? materialRepository.findAllByOrderByNombreAsc() : materialRepository.findByActivoTrueOrderByNombreAsc();
    }

    @Transactional
    public MakerMaterial saveMaterial(Integer id, MakerMaterialRequest request) {
        if ((id == null && materialRepository.existsByNombreIgnoreCase(request.nombre())) ||
                (id != null && materialRepository.existsByNombreIgnoreCaseAndIdNot(request.nombre(), id)))
            throw new RepeatedException("Ya existe un material con ese nombre");
        MakerMaterial material = id == null ? new MakerMaterial() : materialRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Material no encontrado"));
        material.setNombre(request.nombre().trim());
        material.setActivo(request.activo() == null || request.activo());
        return materialRepository.save(material);
    }

    @Transactional
    public void deleteMaterial(Integer id) {
        MakerMaterial m = materialRepository.findById(id).orElseThrow(() -> new NotFoundException("Material no encontrado"));
        m.setActivo(false); materialRepository.save(m);
    }

    public List<MakerCatalogResponse> filaments(boolean all) {
        return (all ? filamentoRepository.findAllByOrderByNombreAsc() : filamentoRepository.findByActivoTrueOrderByNombreAsc())
                .stream().map(this::filamentResponse).toList();
    }

    @Transactional
    public MakerCatalogResponse saveFilament(Integer id, MakerFilamentoRequest request) {
        MakerMaterial material = materialRepository.findById(request.materialId())
                .orElseThrow(() -> new NotFoundException("Material no encontrado"));
        MakerFilamento filament = id == null ? new MakerFilamento() : filamentoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Filamento no encontrado"));
        filament.setNombre(request.nombre().trim()); filament.setMaterial(material);
        filament.setPrecioKilo(request.precioKilo()); filament.setMarca(blankToNull(request.marca()));
        filament.setStockKg(request.stockKg() == null ? BigDecimal.ZERO : request.stockKg().setScale(3, ROUNDING));
        filament.setColor(blankToNull(request.color())); filament.setActivo(request.activo() == null || request.activo());
        return filamentResponse(filamentoRepository.save(filament));
    }

    @Transactional
    public void deleteFilament(Integer id) {
        MakerFilamento f = filamentoRepository.findById(id).orElseThrow(() -> new NotFoundException("Filamento no encontrado"));
        f.setActivo(false); filamentoRepository.save(f);
    }

    public List<MakerConfiguracion> settings() { ensureDefaults(); return configRepository.findAll(Sort.by("clave")); }

    @Transactional
    public MakerConfiguracion updateSetting(String key, MakerConfiguracionRequest request) {
        MakerConfiguracion setting = configRepository.findByClave(key)
                .orElseThrow(() -> new NotFoundException("Configuración no encontrada"));
        setting.setValor(request.valor()); return configRepository.save(setting);
    }

    @Transactional
    public MakerResponse createQuote(MakerCotizacionRequest request, String username) {
        ensureDefaults();
        Calc calc = calculate(request);
        MakerCotizacion quote = MakerCotizacion.builder().folio(nextFolio()).fecha(LocalDateTime.now())
                .usuario(username == null ? null : usuarioRepository.findByUsername(username).orElse(null))
                .descripcion(blankToNull(request.descripcion())).modo(request.modo().toUpperCase(Locale.ROOT))
                .gramosNetos(request.gramosNetos()).gramosMerma(calc.waste).gramosTotales(calc.totalGrams)
                .horasImpresion(request.horasImpresion()).piezasPorCama(request.piezasPorCama())
                .consumoKw(calc.kw).costoKwh(calc.kwh).costoMaterial(calc.materialCost).costoElectricidad(calc.electricCost)
                .costoExtras(calc.extraCost).costoCama(calc.bedCost).costoPieza(calc.unitCost)
                .tipoPrecio(request.tipoPrecio().toUpperCase(Locale.ROOT)).margen(calc.margin)
                .precioCama(calc.saleBed).precioPieza(calc.saleUnit)
                .precioCamaFinal(calc.saleBed).precioPiezaFinal(calc.saleUnit).build();
        if (request.proyectoId() != null) quote.setProyecto(proyectoRepository.findById(request.proyectoId())
                .orElseThrow(() -> new NotFoundException("Proyecto no encontrado")));
        for (MakerFilamentoInput input : request.filamentos()) {
            MakerFilamento f = filamentoRepository.findById(input.filamentoId()).orElseThrow(() -> new NotFoundException("Filamento no encontrado"));
            BigDecimal lineCost = lineCost(f.getPrecioKilo(), input.gramos(), request.gramosNetos(), calc.waste, request.modo(), request.cambiosColor());
            quote.getFilamentos().add(MakerCotizacionFilamento.builder().cotizacion(quote).filamento(f)
                    .nombreFilamento(f.getNombre()).material(f.getMaterial().getNombre()).marca(f.getMarca()).color(f.getColor())
                    .precioKilo(f.getPrecioKilo()).gramos(input.gramos()).costo(lineCost).build());
        }
        if (request.extras() != null) for (MakerExtraInput extra : request.extras()) {
            BigDecimal amount = extra.cantidad().multiply(extra.precioUnitario()).setScale(SCALE, ROUNDING);
            quote.getExtras().add(MakerCotizacionExtra.builder().cotizacion(quote).nombre(extra.nombre().trim())
                    .cantidad(extra.cantidad()).precioUnitario(extra.precioUnitario()).importe(amount).build());
        }
        MakerCotizacion saved = cotizacionRepository.save(quote);
        if (saved.getProyecto() != null) {
            saved.getProyecto().setCotizacion(saved);
            proyectoRepository.save(saved.getProyecto());
        }
        return response(saved);
    }

    @Transactional(readOnly = true)
    public Page<MakerResponse> quotes(Pageable pageable) { return cotizacionRepository.findAllByOrderByFechaDesc(pageable).map(this::response); }

    @Transactional(readOnly = true)
    public MakerResponse quote(Integer id) { return response(cotizacionRepository.findById(id).orElseThrow(() -> new NotFoundException("Cotización no encontrada"))); }

    @Transactional
    public void deleteQuote(Integer id) { if (!cotizacionRepository.existsById(id)) throw new NotFoundException("Cotización no encontrada"); proyectoRepository.findByCotizacionId(id).forEach(p -> { p.setCotizacion(null); proyectoRepository.save(p); }); cotizacionRepository.deleteById(id); }

    @Transactional
    public MakerResponse updateFinalPrice(Integer id, MakerPrecioFinalRequest request) {
        MakerCotizacion quote = cotizacionRepository.findById(id).orElseThrow(() -> new NotFoundException("Cotización no encontrada"));
        BigDecimal finalUnit = request.precioPiezaFinal().setScale(2, ROUNDING);
        quote.setPrecioPiezaFinal(finalUnit);
        quote.setPrecioCamaFinal(finalUnit.multiply(BigDecimal.valueOf(quote.getPiezasPorCama())).setScale(2, ROUNDING));
        return response(cotizacionRepository.save(quote));
    }

    private Calc calculate(MakerCotizacionRequest request) {
        String mode = request.modo().toUpperCase(Locale.ROOT);
        if (!mode.equals("MONOCOLOR") && !mode.equals("MULTICOLOR"))
            throw new IllegalArgumentException("El modo debe ser MONOCOLOR o MULTICOLOR");
        String type = request.tipoPrecio().toUpperCase(Locale.ROOT);
        if (!Set.of("PUBLICO", "MAYOREO", "DISTRIBUIDOR").contains(type))
            throw new IllegalArgumentException("El tipo de precio no es válido");
        BigDecimal filamentGrams = request.filamentos().stream().map(MakerFilamentoInput::gramos).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (filamentGrams.subtract(request.gramosNetos()).abs().compareTo(new BigDecimal("0.01")) > 0)
            throw new IllegalArgumentException("La suma de gramos de los filamentos debe coincidir con los gramos netos");
        BigDecimal rate = config(mode.equals("MULTICOLOR") ? "MERMA_MULTICOLOR" : "MERMA_MONOCOLOR");
        BigDecimal purge = mode.equals("MULTICOLOR") ? config("PURGA_POR_CAMBIO").multiply(BigDecimal.valueOf(Math.max(0, Optional.ofNullable(request.cambiosColor()).orElse(0)))) : BigDecimal.ZERO;
        BigDecimal waste = request.gramosNetos().multiply(rate).add(purge).setScale(3, ROUNDING);
        BigDecimal total = request.gramosNetos().add(waste).setScale(3, ROUNDING);
        BigDecimal material = BigDecimal.ZERO;
        for (MakerFilamentoInput line : request.filamentos()) {
            MakerFilamento f = filamentoRepository.findById(line.filamentoId()).orElseThrow(() -> new NotFoundException("Filamento no encontrado"));
            material = material.add(lineCost(f.getPrecioKilo(), line.gramos(), request.gramosNetos(), waste, mode, request.cambiosColor()));
        }
        material = material.setScale(SCALE, ROUNDING);
        BigDecimal kw = config("CONSUMO_KW");
        BigDecimal kwh = config("COSTO_KWH");
        BigDecimal electricity = kw.multiply(request.horasImpresion()).multiply(kwh).setScale(SCALE, ROUNDING);
        BigDecimal extras = Optional.ofNullable(request.extras()).orElse(List.of()).stream()
                .map(e -> e.cantidad().multiply(e.precioUnitario())).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(SCALE, ROUNDING);
        BigDecimal bed = material.add(electricity).add(extras).setScale(SCALE, ROUNDING);
        BigDecimal unit = bed.divide(BigDecimal.valueOf(request.piezasPorCama()), SCALE, ROUNDING);
        BigDecimal margin = config("MARGEN_" + type);
        if (margin.compareTo(BigDecimal.ONE) >= 0) throw new IllegalArgumentException("El margen debe ser menor a 100%");
        BigDecimal saleBed = bed.divide(BigDecimal.ONE.subtract(margin), SCALE, ROUNDING);
        return new Calc(waste, total, kw, kwh, material, electricity, extras, bed, unit, margin, saleBed,
                saleBed.divide(BigDecimal.valueOf(request.piezasPorCama()), SCALE, ROUNDING));
    }

    private BigDecimal lineCost(BigDecimal price, BigDecimal grams, BigDecimal net, BigDecimal waste, String mode, Integer changes) {
        BigDecimal lineWaste = net.signum() == 0 ? BigDecimal.ZERO : waste.multiply(grams).divide(net, 6, ROUNDING);
        return grams.add(lineWaste).multiply(price).divide(BigDecimal.valueOf(1000), SCALE, ROUNDING);
    }

    private BigDecimal config(String key) { return configRepository.findByClave(key).map(MakerConfiguracion::getValor).orElse(BigDecimal.ZERO); }
    private String nextFolio() { return "MK-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")); }
    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private MakerCatalogResponse filamentResponse(MakerFilamento f) { return new MakerCatalogResponse(f.getId(), f.getNombre(), f.getMaterial().getNombre(), f.getMaterial().getId(), f.getPrecioKilo(), f.getStockKg(), f.getMarca(), f.getColor(), f.getActivo()); }

    private MakerResponse response(MakerCotizacion q) {
        return new MakerResponse(q.getId(), q.getFolio(), q.getFecha(), q.getUsuario() == null ? null : q.getUsuario().getUsername(), q.getDescripcion(), q.getModo(), q.getGramosNetos(), q.getGramosMerma(), q.getGramosTotales(), q.getHorasImpresion(), q.getPiezasPorCama(), q.getConsumoKw(), q.getCostoKwh(), q.getCostoMaterial(), q.getCostoElectricidad(), q.getCostoExtras(), q.getCostoCama(), q.getCostoPieza(), q.getTipoPrecio(), q.getMargen(), q.getPrecioCama(), q.getPrecioPieza(), q.getFilamentos().stream().map(f -> new MakerResponse.MakerFilamentoResponse(f.getNombreFilamento(), f.getMaterial(), f.getMarca(), f.getColor(), f.getGramos(), f.getPrecioKilo(), f.getCosto())).toList(), q.getExtras().stream().map(e -> new MakerResponse.MakerExtraResponse(e.getNombre(), e.getCantidad(), e.getPrecioUnitario(), e.getImporte())).toList(), q.getPrecioCamaFinal(), q.getPrecioPiezaFinal());
    }
    private record Calc(BigDecimal waste, BigDecimal totalGrams, BigDecimal kw, BigDecimal kwh, BigDecimal materialCost, BigDecimal electricCost, BigDecimal extraCost, BigDecimal bedCost, BigDecimal unitCost, BigDecimal margin, BigDecimal saleBed, BigDecimal saleUnit) {}
}
