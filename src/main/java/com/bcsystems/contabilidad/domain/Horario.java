package com.bcsystems.contabilidad.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "horario")
public class Horario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;
    @Column(nullable = false, length = 100) private String nombre;
    @Column(nullable = false) private Integer toleranciaEntradaMinutos = 0;
    @Column(nullable = false) private Integer toleranciaSalidaMinutos = 0;
    @Column(nullable = false) private Boolean activo = true;
    @OneToMany(mappedBy = "horario", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default private List<HorarioDia> dias = new ArrayList<>();
}
