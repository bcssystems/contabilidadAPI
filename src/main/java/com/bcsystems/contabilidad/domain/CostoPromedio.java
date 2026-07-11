package com.bcsystems.contabilidad.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "costo_promedio")
public class CostoPromedio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_padre_id", nullable = false, unique = true)
    private ProductoPadre productoPadre;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal costoPromedio;

    @Column(nullable = false)
    @Builder.Default
    private Integer cantidadTotal = 0;

    @Builder.Default
    private LocalDateTime ultimaActualizacion = LocalDateTime.now();
}
