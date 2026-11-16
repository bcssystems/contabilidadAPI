package com.bcsystems.contabilidad.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Table(name = "maker_cotizacion", indexes = {
        @Index(name = "idx_maker_cotizacion_fecha", columnList = "fecha"),
        @Index(name = "idx_maker_cotizacion_folio", columnList = "folio")
})
public class MakerCotizacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 30)
    private String folio;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id")
    private MakerProyecto proyecto;

    @Column(length = 160)
    private String descripcion;

    @Column(nullable = false, length = 20)
    private String modo;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal gramosNetos;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal gramosMerma;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal gramosTotales;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal horasImpresion;

    @Column(nullable = false)
    private Integer piezasPorCama;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal consumoKw;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal costoKwh;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costoMaterial;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costoElectricidad;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costoExtras;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costoCama;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costoPieza;

    @Column(nullable = false, length = 20)
    private String tipoPrecio;

    @Column(nullable = false, precision = 6, scale = 4)
    private BigDecimal margen;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioCama;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioPieza;

    @Column(precision = 12, scale = 2)
    private BigDecimal precioCamaFinal;

    @Column(precision = 12, scale = 2)
    private BigDecimal precioPiezaFinal;

    @Builder.Default
    @OneToMany(mappedBy = "cotizacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MakerCotizacionFilamento> filamentos = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "cotizacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MakerCotizacionExtra> extras = new ArrayList<>();
}
