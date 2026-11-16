package com.bcsystems.contabilidad.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Table(name = "maker_proyecto", indexes = {
        @Index(name = "idx_maker_proyecto_estado", columnList = "estado"),
        @Index(name = "idx_maker_proyecto_fecha", columnList = "fecha_fin")
})
public class MakerProyecto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 160)
    private String nombre;

    @Lob
    private String descripcion;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(nullable = false, length = 20)
    private String prioridad;

    @Column(nullable = false, length = 20)
    private String color;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    @Builder.Default
    private Boolean filamentoDescontado = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario creador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cotizacion_id")
    private MakerCotizacion cotizacion;

    @ManyToMany
    @JoinTable(name = "maker_proyecto_usuario",
            joinColumns = @JoinColumn(name = "proyecto_id"),
            inverseJoinColumns = @JoinColumn(name = "usuario_id"))
    @Builder.Default
    private List<Usuario> asignados = new ArrayList<>();
}
