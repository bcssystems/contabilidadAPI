package com.bcsystems.contabilidad.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "inventario_sucursal", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"id_producto", "id_sucursal"})
})
public class InventarioSucursal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto")
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variante_id")
    private Variante variante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sucursal", nullable = false)
    private Sucursal sucursal;

    @Column(nullable = false)
    private Integer stock;

    private Integer stockMinimo;

    private Integer stockMaximo;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;
}
