package com.bcsystems.contabilidad.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "variante_atributo", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"id_variante", "id_atributo"})
})
public class VarianteAtributo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_variante", nullable = false)
    private Variante variante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_atributo", nullable = false)
    private Atributo atributo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_valor", nullable = false)
    private AtributoValor valor;
}
