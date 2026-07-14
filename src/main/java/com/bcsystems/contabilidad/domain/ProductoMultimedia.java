package com.bcsystems.contabilidad.domain;

import com.bcsystems.contabilidad.domain.en.TipoMultimedia;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "producto_multimedia")
public class ProductoMultimedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idMultimedia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    @JsonIgnore
    private Producto producto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMultimedia tipo;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(length = 255)
    private String nombreArchivo;

    @Column(nullable = false)
    private Boolean esPrincipal;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaSubida;
}
