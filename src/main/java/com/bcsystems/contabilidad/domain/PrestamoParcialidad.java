package com.bcsystems.contabilidad.domain;

import com.bcsystems.contabilidad.domain.en.EstadoParcialidad;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "prestamo_parcialidad", uniqueConstraints = @UniqueConstraint(name = "uk_prestamo_parcialidad_numero", columnNames = {"prestamo_id", "numero"}))
public class PrestamoParcialidad {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idParcialidad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prestamo_id", nullable = false)
    private PrestamoEmpleado prestamo;

    @Column(nullable = false)
    private Integer numero;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal capital;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal interes;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal saldo;
    @Column(nullable = false)
    private LocalDate fechaVencimiento;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoParcialidad estado;
}
