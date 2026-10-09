package com.bcsystems.contabilidad.domain;

import com.bcsystems.contabilidad.domain.en.EstadoComision;
import com.bcsystems.contabilidad.domain.en.TipoComision;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
@Table(name = "comision")
public class Comision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idComision;
    @Enumerated(EnumType.STRING)
    private TipoComision tipoComision;
    @Column(length = 200)
    private String concepto;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id")
    @JsonBackReference
    private Venta venta;
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
    private BigDecimal montoBase;
    private BigDecimal porcentajeComision;
    private BigDecimal montoComision;
    @Enumerated(EnumType.STRING)
    private EstadoComision estadoComision;
    private LocalDate fechaComision = LocalDate.now();
    private LocalDate fechaPago;
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "nomina_id")
    private Nomina nomina;
}
