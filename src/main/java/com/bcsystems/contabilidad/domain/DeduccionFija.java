package com.bcsystems.contabilidad.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "deduccion_fija")
public class DeduccionFija {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDeduccionFija;

    @Column(nullable = false, length = 200)
    private String concepto;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    @ManyToMany
    @JoinTable(name = "deduccion_fija_usuario",
            joinColumns = @JoinColumn(name = "deduccion_fija_id"),
            inverseJoinColumns = @JoinColumn(name = "usuario_id"))
    @Builder.Default
    private Set<Usuario> usuarios = new HashSet<>();
}
