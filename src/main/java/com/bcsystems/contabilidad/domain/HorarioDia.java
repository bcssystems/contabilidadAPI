package com.bcsystems.contabilidad.domain;

import com.bcsystems.contabilidad.domain.en.DiaSemana;
import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "horario_dia", uniqueConstraints = @UniqueConstraint(columnNames = {"id_horario", "dia"}))
public class HorarioDia {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;
    @ManyToOne(optional = false) @JoinColumn(name = "id_horario") @JsonIgnore private Horario horario;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private DiaSemana dia;
    @Column(nullable = false) private LocalTime entrada;
    @Column(nullable = false) private LocalTime salida;
    @OneToMany(mappedBy = "horarioDia", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default private List<HorarioBreak> breaks = new ArrayList<>();
}
