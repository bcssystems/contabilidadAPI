package com.bcsystems.contabilidad.domain;

import com.bcsystems.contabilidad.domain.en.TipoAsientoContable;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "asiento_contable")
public class AsientoContable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAsientoContable;
    @Column(nullable = false)
    private LocalDate fecha;
    @Column(length = 100, nullable = false)
    private String referencia;
    @Column(nullable = false)
    private String descripcion;
    @Enumerated(EnumType.STRING)
    private TipoAsientoContable tipoAsientoContable;
    @OneToMany(mappedBy = "asientoContable", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<MovimientoContable> movimientos = new ArrayList<>();
}
