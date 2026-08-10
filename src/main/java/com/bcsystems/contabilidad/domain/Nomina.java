package com.bcsystems.contabilidad.domain;

import com.bcsystems.contabilidad.domain.en.EstadoNomina;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "nomina")
public class Nomina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idNomina;
    private LocalDate fecha_nomina;
    private BigDecimal sueldo;
    private BigDecimal sueldoBase;
    private BigDecimal totalComisiones;
    private BigDecimal totalDeducciones;
    @Enumerated(EnumType.STRING)
    private EstadoNomina estado;
    @ManyToOne
    @JoinColumn(name = "empleado_id")
    private Empleado empleado;
    @ManyToOne
    @JoinColumn(name = "asiento_id")
    private AsientoContable asientoContable;
    @OneToMany(mappedBy = "nomina")
    @JsonIgnore
    private List<Comision> comisiones;
    @OneToMany(mappedBy = "nomina")
    @JsonIgnore
    private List<Deduccion> deducciones;
}
