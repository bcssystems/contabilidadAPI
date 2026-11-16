package com.bcsystems.contabilidad.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Table(name = "maker_cotizacion_filamento")
public class MakerCotizacionFilamento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cotizacion_id", nullable = false)
    private MakerCotizacion cotizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filamento_id")
    private MakerFilamento filamento;

    @Column(nullable = false, length = 150)
    private String nombreFilamento;

    @Column(length = 100)
    private String material;

    @Column(length = 100)
    private String marca;

    @Column(length = 80)
    private String color;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioKilo;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal gramos;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costo;
}
