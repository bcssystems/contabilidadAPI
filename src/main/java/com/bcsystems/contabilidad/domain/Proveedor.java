package com.bcsystems.contabilidad.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "proveedor")
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProveedor;
    private String nombre;
    @ManyToOne
    @JoinColumn(name = "cuenta_por_pagar_id")
    private CuentaContable cuentaPorPagar;

}
