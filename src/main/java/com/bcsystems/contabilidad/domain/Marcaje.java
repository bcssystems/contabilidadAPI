package com.bcsystems.contabilidad.domain;

import com.bcsystems.contabilidad.domain.en.*;
import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "marcaje")
public class Marcaje {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;
    @ManyToOne(optional = false) @JoinColumn(name = "id_asistencia") @JsonIgnore private Asistencia asistencia;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private TipoMarcaje tipo;
    @Column(nullable = false) private LocalDateTime marcadoEn;
    @Column(nullable = false, precision = 10, scale = 7) private BigDecimal latitud;
    @Column(nullable = false, precision = 10, scale = 7) private BigDecimal longitud;
    private BigDecimal precisionMetros;
    private Double distanciaMetros;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private EstadoIncidencia estadoUbicacion;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "punto_geografico_id") @JsonIgnore private PuntoGeografico puntoGeografico;
}
