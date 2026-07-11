package com.bcsystems.contabilidad.domain;

import com.bcsystems.contabilidad.domain.en.EstadoMovimiento;
import com.bcsystems.contabilidad.domain.en.TipoMovimiento;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "movimiento_almacen")
public class MovimientoAlmacen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimiento tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variante_id", nullable = false)
    private Variante variante;

    @Builder.Default
    private Integer cantidad = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Usuario solicitante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aprobador_id")
    private Usuario aprobador;

    @Column(nullable = false)
    private LocalDateTime fechaSolicitud;

    private LocalDateTime fechaAprobacion;

    private LocalDateTime fechaMovimiento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EstadoMovimiento estado = EstadoMovimiento.PENDIENTE;

    @Column(columnDefinition = "TEXT")
    private String observacion;

    @Column(length = 100)
    private String valeFolio;

    @Column(length = 100)
    private String folioVenta;

    @Column(length = 100)
    private String medioCompra;

    @Column(length = 100)
    private String proveedorNombre;

    @Column(precision = 10, scale = 2)
    private BigDecimal precioCompra;

    private String imei;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_id")
    private Sucursal sucursal;
}
