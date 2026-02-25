package com.bcsystems.contabilidad.domain;

import com.bcsystems.contabilidad.domain.en.EstadoComision;
import com.bcsystems.contabilidad.domain.en.TipoComision;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "comision")
public class Comision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idComision;
    @Enumerated(EnumType.STRING)
    private TipoComision tipoComision;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id")
    private Venta venta;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;
    private BigDecimal montoBase;
    private BigDecimal porcentajeComision;
    private BigDecimal montoComision;
    @Enumerated(EnumType.STRING)
    private EstadoComision estadoComision;
    private LocalDate fechaComision = LocalDate.now();
    private LocalDate fechaPago;

}
