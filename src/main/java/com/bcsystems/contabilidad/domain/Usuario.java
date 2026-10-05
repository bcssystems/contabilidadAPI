package com.bcsystems.contabilidad.domain;

import com.bcsystems.contabilidad.domain.en.Role;
import com.bcsystems.contabilidad.domain.en.Position;
import java.math.BigDecimal;
import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

/*
* En esta clase se modela al usuario, el cual va a estar ingresando al sistema, tenemos el username,
* password, activo, rol, y se liga a persona. todo(realizar la auth)
* */

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idUser;
    @Column(unique = true, nullable = false, length = 50)
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private Boolean activo;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;
    @ManyToOne
    @JoinColumn(name = "id_person")
    private Persona persona;
    @Enumerated(EnumType.STRING)
    private Position position;
    @Column(precision = 12, scale = 2)
    private BigDecimal sueldoBase;
    @Column(precision = 12, scale = 2)
    private BigDecimal sueldoSemanal;
}
