package com.bcsystems.contabilidad.domain;

import com.bcsystems.contabilidad.domain.en.EstadoDeduccion;
import com.bcsystems.contabilidad.domain.en.OrigenDeduccion;
import com.bcsystems.contabilidad.domain.en.TipoDeduccion;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "deduccion")
public class Deduccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDeduccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(length = 200)
    private String concepto;

    @Column(precision = 12, scale = 2)
    private BigDecimal monto;

    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    private EstadoDeduccion estado;

    @Enumerated(EnumType.STRING)
    private TipoDeduccion tipo;

    @Enumerated(EnumType.STRING)
    private OrigenDeduccion origen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "asistencia_id")
    private Asistencia asistencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corte_id")
    private CorteCaja corteCaja;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "nomina_id")
    private Nomina nomina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestamo_parcialidad_id", unique = true)
    private PrestamoParcialidad prestamoParcialidad;
}
