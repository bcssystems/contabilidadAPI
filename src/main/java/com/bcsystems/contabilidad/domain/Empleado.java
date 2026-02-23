package com.bcsystems.contabilidad.domain;

import com.bcsystems.contabilidad.domain.en.Position;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/*
* Definimos la estructura de los empleados, los cuales tienen id, salario base, fecha de ingreso, activo o no activo,
* posicion, se liga a la persona correspondiente.
* */

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "employee")
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idEmployee;
    @Column(nullable = false)
    private Float baseSalary;
    @Column(nullable = false)
    private LocalDate fechaIngreso;
    @Column(nullable = false)
    private Boolean activo = true;
    @Enumerated(EnumType.STRING)
    private Position position;
    @OneToOne
    @JoinColumn(name = "id_person")
    private Persona persona;

}
