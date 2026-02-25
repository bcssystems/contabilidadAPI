package com.bcsystems.contabilidad.domain;

import com.bcsystems.contabilidad.domain.en.EstadoVenta;
import com.bcsystems.contabilidad.domain.en.TipoVenta;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "venta")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idVenta;
    private LocalDate fecha_venta;
    @Enumerated(EnumType.STRING)
    private TipoVenta tipoVenta;
    @Enumerated(EnumType.STRING)
    private EstadoVenta estadoVenta;
    private BigDecimal total;
    private BigDecimal costo;
    @ManyToOne
    @JoinColumn(name = "asiento_id")
    private AsientoContable asiento;
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Comision> comisiones = new ArrayList<>();

}
