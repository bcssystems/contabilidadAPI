package com.bcsystems.contabilidad.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record MakerResponse(Integer id, String folio, LocalDateTime fecha, String usuario, String descripcion,
                            String modo, BigDecimal gramosNetos, BigDecimal gramosMerma, BigDecimal gramosTotales,
                            BigDecimal horasImpresion, Integer piezasPorCama, BigDecimal consumoKw, BigDecimal costoKwh,
                            BigDecimal costoMaterial, BigDecimal costoElectricidad, BigDecimal costoExtras,
                            BigDecimal costoCama, BigDecimal costoPieza, String tipoPrecio, BigDecimal margen,
                            BigDecimal precioCama, BigDecimal precioPieza, List<MakerFilamentoResponse> filamentos,
                            List<MakerExtraResponse> extras, BigDecimal precioCamaFinal, BigDecimal precioPiezaFinal) {
    public record MakerFilamentoResponse(String nombre, String material, String marca, String color,
                                         BigDecimal gramos, BigDecimal precioKilo, BigDecimal costo) {}
    public record MakerExtraResponse(String nombre, BigDecimal cantidad, BigDecimal precioUnitario, BigDecimal importe) {}
}
