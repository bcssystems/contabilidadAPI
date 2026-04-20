package com.bcsystems.contabilidad.domain;

import com.bcsystems.contabilidad.domain.en.PersonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/*
* En esta clase modelamos a una persona, con los atributos basicos, nombre, primer apellido, segundo
* apellido (nulleable), tipo de persona (empleado/usuario), y y la fecha de creacion
* */

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "persona")
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPerson;
    @Column(nullable = false, length = 30)
    private String nombre;
    @Column(nullable = false, length = 30)
    private String primerApellido;
    @Column(nullable = true, length = 30)
    private String segundoApellido;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PersonType personType;
    @CreationTimestamp
    @Column(name = "fecha_registro", updatable = false, nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();
}
