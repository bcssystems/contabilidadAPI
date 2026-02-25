package com.bcsystems.contabilidad.domain;

import com.bcsystems.contabilidad.domain.en.EstadoCompra;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "compra")
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCompra;
    private LocalDate fecha_compra;
    private BigDecimal total_compra;
    @Enumerated(EnumType.STRING)
    private EstadoCompra estadoCompra;
    @ManyToOne
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;
    @ManyToOne
    @JoinColumn(name = "asiento_id")
    private AsientoContable asientoContable;
}
