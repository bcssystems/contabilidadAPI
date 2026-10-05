package com.bcsystems.contabilidad.domain;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "empleado_horario")
public class EmpleadoHorario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;
    @ManyToOne(optional = false) @JoinColumn(name = "id_empleado") @JsonIgnore private Empleado empleado;
    @ManyToOne(optional = false) @JoinColumn(name = "id_horario") private Horario horario;
    @Column(nullable = false) private LocalDate vigenteDesde;
    private LocalDate vigenteHasta;
    @Column(nullable = false) private Boolean activo = true;
}
