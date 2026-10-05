package com.bcsystems.contabilidad.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "punto_geografico")
public class PuntoGeografico {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;
    @Column(nullable = false, length = 100) private String nombre;
    @Column(nullable = false, precision = 10, scale = 7) private BigDecimal latitud;
    @Column(nullable = false, precision = 10, scale = 7) private BigDecimal longitud;
    @Column(nullable = false) private Integer radioMetros = 100;
    @Column(nullable = false) private Boolean activo = true;
}
