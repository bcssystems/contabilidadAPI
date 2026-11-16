package com.bcsystems.contabilidad.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Table(name = "maker_tarea", indexes = {
        @Index(name = "idx_maker_tarea_estado", columnList = "estado"),
        @Index(name = "idx_maker_tarea_proyecto", columnList = "proyecto_id")
})
public class MakerTarea {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 180)
    private String nombre;

    @Lob
    private String descripcion;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String checklist;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id")
    private MakerProyecto proyecto;

    @ManyToMany
    @JoinTable(name = "maker_tarea_usuario",
            joinColumns = @JoinColumn(name = "tarea_id"),
            inverseJoinColumns = @JoinColumn(name = "usuario_id"))
    @Builder.Default
    private List<Usuario> asignados = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "maker_tarea_mencion",
            joinColumns = @JoinColumn(name = "tarea_id"),
            inverseJoinColumns = @JoinColumn(name = "mencionada_id"))
    @Builder.Default
    private List<MakerTarea> tareasMencionadas = new ArrayList<>();
}
