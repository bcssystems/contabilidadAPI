package com.bcsystems.contabilidad.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonBackReference
    @JoinColumn(name = "asiento_id")
    private AsientoContable asientoContable;
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "cuenta_id")
    private CuentaContable cuentaContable;

}
