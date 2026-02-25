package com.bcsystems.contabilidad.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "movimiento_contable")
public class MovimientoContable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMovimientoContable;
    private BigDecimal debe = BigDecimal.ZERO;
    private BigDecimal haber = BigDecimal.ZERO;
    @ManyToOne
    @JoinColumn(name = "asiento_id")
    private AsientoContable asientoContable;
    @ManyToOne
    @JoinColumn(name = "cuenta_id")
    private CuentaContable cuentaContable;

}
