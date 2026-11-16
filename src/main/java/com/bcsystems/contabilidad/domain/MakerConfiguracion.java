package com.bcsystems.contabilidad.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Table(name = "maker_configuracion", uniqueConstraints = @UniqueConstraint(name = "uk_maker_config_clave", columnNames = "clave"))
public class MakerConfiguracion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 80)
    private String clave;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal valor;

    @Column(nullable = false, length = 30)
    private String unidad;

    @Column(length = 255)
    private String descripcion;
}
