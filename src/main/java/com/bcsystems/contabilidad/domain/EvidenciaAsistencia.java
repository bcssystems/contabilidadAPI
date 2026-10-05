package com.bcsystems.contabilidad.domain;

import com.bcsystems.contabilidad.domain.en.EstadoEvidencia;
import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "evidencia_asistencia")
public class EvidenciaAsistencia {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;
    @OneToOne(optional = false) @JoinColumn(name = "id_marcaje", unique = true) @JsonIgnore private Marcaje marcaje;
    @Column(nullable = false, length = 255) private String nombreArchivo;
    @Column(nullable = false, length = 500) private String rutaArchivo;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private EstadoEvidencia estado = EstadoEvidencia.PENDIENTE;
    @Column(nullable = false) private LocalDateTime creadoEn;
}
