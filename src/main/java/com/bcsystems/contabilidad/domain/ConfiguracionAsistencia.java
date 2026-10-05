package com.bcsystems.contabilidad.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "configuracion_asistencia")
public class ConfiguracionAsistencia {
    @Id private Integer id;
    @Column(nullable = false) private Integer toleranciaEntrada = 0;
    @Column(nullable = false) private Integer toleranciaSalida = 0;
    @Column(nullable = false) private Integer radioMaximo = 100;
    @Column(nullable = false) private Boolean requiereSelfie = true;
}
