package com.bcsystems.contabilidad.domain;

import com.bcsystems.contabilidad.domain.en.TipoAbono;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "abono")
public class Abono {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idAbono;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_credito", nullable = false)
    private Credito credito;

    @Column(nullable = false)
    private Double monto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TipoAbono tipo;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Persona usuario;
}
