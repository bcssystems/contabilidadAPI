package com.bcsystems.contabilidad.domain;

import com.bcsystems.contabilidad.domain.en.EstadoJustificacion;
import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "justificacion_asistencia")
public class JustificacionAsistencia {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;
    @ManyToOne(optional = false) @JoinColumn(name = "id_usuario") @JsonIgnore private Usuario usuario;
    @ManyToOne @JoinColumn(name = "id_asistencia") @JsonIgnore private Asistencia asistencia;
    @Column(nullable = false, length = 2000) private String comentario;
    private String nombreArchivo;
    private String rutaArchivo;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private EstadoJustificacion estado = EstadoJustificacion.PENDIENTE;
    @Column(nullable = false) private LocalDateTime creadoEn;
}
