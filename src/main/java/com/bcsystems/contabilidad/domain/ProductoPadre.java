package com.bcsystems.contabilidad.domain;

import com.bcsystems.contabilidad.domain.en.CategoriaProducto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "producto_padre")
public class ProductoPadre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String sku;

    @Column(nullable = false)
    private String nombre;

    private String marca;

    private String modelo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaProducto categoria;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(length = 500)
    private String mensajeWhatsapp;

    @Builder.Default
    private Boolean tieneVariantes = false;

    @Builder.Default
    private Boolean activo = true;

    @Builder.Default
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}
