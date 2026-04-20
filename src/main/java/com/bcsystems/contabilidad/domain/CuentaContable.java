package com.bcsystems.contabilidad.domain;

import com.bcsystems.contabilidad.domain.en.TipoCuentaContable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "cuenta_contable")
public class CuentaContable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCuentaContable;
    @Column(length = 20, nullable = false, unique = true)
    private String codigo;
    @Column(nullable = false)
    private String nombre;
    @Enumerated(EnumType.STRING)
    private TipoCuentaContable tipoCuentaContable;
    private Boolean activa = true;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_padre_id")
    private CuentaContable cuentaPadre;
    @OneToMany(mappedBy = "cuentaPadre",
            cascade = CascadeType.ALL,
            orphanRemoval = false)
    private List<CuentaContable> subcuentas;
}
