package com.bcsystems.contabilidad.domain;

import com.bcsystems.contabilidad.domain.en.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "variante")
public class Variante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_padre_id", nullable = false)
    private ProductoPadre productoPadre;

    private String nombre;

    @Column(name = "numero_serie", unique = true, nullable = false)
    private String numeroSerie;

    private LocalDate fechaEntrada;

    private Integer porcentajeBateria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;

    @Enumerated(EnumType.STRING)
    private TipoCompraProducto tipoCompra;

    private String ubicacion;

    private String imei;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatusProducto status = StatusProducto.DISPONIBLE;

    @Enumerated(EnumType.STRING)
    private EstadoProducto estado;

    @Column(precision = 10, scale = 2)
    private BigDecimal precioCompra;

    @Column(precision = 10, scale = 2)
    private BigDecimal precioPublico;

    @Column(precision = 10, scale = 2)
    private BigDecimal precioConsigna;

    @Column(precision = 10, scale = 2)
    private BigDecimal precioMayoreo;

    private String valeFolioSalida;

    private String folioVenta;

    @Column(columnDefinition = "TEXT")
    private String observacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private Usuario responsable;

    @Column(columnDefinition = "TEXT")
    private String imagenes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_id")
    private Sucursal sucursal;

    @Builder.Default
    private Boolean pagadoCaja = false;

    @OneToMany(mappedBy = "variante", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<InventarioSucursal> inventarioSucursales = new ArrayList<>();

    @OneToMany(mappedBy = "variante", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<VarianteAtributo> varianteAtributos = new ArrayList<>();

    @Builder.Default
    private Boolean activo = true;

    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}
