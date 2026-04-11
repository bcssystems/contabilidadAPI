package com.bcsystems.contabilidad.domain;

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
@Table(name = "nomina")
public class Nomina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idNomina;
    private LocalDate fecha_nomina;
    private BigDecimal sueldo;
    @ManyToOne
    @JoinColumn(name = "empleado_id")
    private Empleado empleado;
    @ManyToOne
    @JoinColumn(name = "asiento_id")
    private AsientoContable asientoContable;
}
