package com.bcsystems.contabilidad.domain;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "asistencia", uniqueConstraints = @UniqueConstraint(columnNames = {"id_usuario", "fecha"}))
public class Asistencia {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;
    @ManyToOne(optional = false) @JoinColumn(name = "id_usuario") @JsonIgnore private Usuario usuario;
    @Column(nullable = false) private LocalDate fecha;
    private LocalDateTime entrada;
    private LocalDateTime salida;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private com.bcsystems.contabilidad.domain.en.EstadoIncidencia estado = com.bcsystems.contabilidad.domain.en.EstadoIncidencia.PENDIENTE;
}
