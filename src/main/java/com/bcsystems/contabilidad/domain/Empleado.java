package com.bcsystems.contabilidad.domain;

import com.bcsystems.contabilidad.domain.en.Position;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

/*
* Definimos la estructura de los empleados, los cuales tienen id, salario base, fecha de ingreso, activo o no activo,
* posicion, se liga a la persona correspondiente.
* */

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "empleado")
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idEmpleado;
    @Column(nullable = false)
    private LocalDate fechaIngreso = LocalDate.now();
    @Column(nullable = false)
    private Boolean activo;
    @Enumerated(EnumType.STRING)
    private Position position;
    @OneToOne
    @JoinColumn(name = "id_person")
    private Persona persona;
    @OneToMany(mappedBy = "empleado")
    private List<Comision> comisiones;

}
