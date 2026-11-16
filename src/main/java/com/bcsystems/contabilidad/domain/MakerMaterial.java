package com.bcsystems.contabilidad.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Table(name = "maker_material", uniqueConstraints = @UniqueConstraint(name = "uk_maker_material_nombre", columnNames = "nombre"))
public class MakerMaterial {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false)
    private Boolean activo;
}
