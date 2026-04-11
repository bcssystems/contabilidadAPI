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
@Table(name = "gasto")
public class Gasto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idGasto;
    private LocalDate fecha_gasto;
    private String concepto;
    private BigDecimal monto;
    @ManyToOne
    @JoinColumn(name = "cuenta_gasto_id")
    private CuentaContable cuentaContable;
    @ManyToOne
    @JoinColumn(name = "asiento_id")
    private AsientoContable asientoContable;
}
