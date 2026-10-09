package com.bcsystems.contabilidad.domain;

import com.bcsystems.contabilidad.domain.en.EstadoPrestamo;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "prestamo_empleado")
public class PrestamoEmpleado {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPrestamo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montoPrincipal;

    @Column(precision = 7, scale = 4)
    private BigDecimal tasaInteresPorcentaje;

    @Column(nullable = false)
    private Integer numeroParcialidades;

    @Column(nullable = true)
    private LocalDateTime fechaCreacion;

    @Column(nullable = true)
    private LocalDate fechaPrimerPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPrestamo estado;

    @OneToMany(mappedBy = "prestamo", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("numero ASC")
    @Builder.Default
    private List<PrestamoParcialidad> parcialidades = new ArrayList<>();
}
