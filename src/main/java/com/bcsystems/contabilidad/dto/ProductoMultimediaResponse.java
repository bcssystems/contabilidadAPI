package com.bcsystems.contabilidad.dto;

import com.bcsystems.contabilidad.domain.ProductoMultimedia;
import com.bcsystems.contabilidad.domain.en.TipoMultimedia;

import java.time.LocalDateTime;

public record ProductoMultimediaResponse(
    Integer idMultimedia,
    TipoMultimedia tipo,
    String url,
    String nombreArchivo,
    Boolean esPrincipal,
    LocalDateTime fechaSubida
) {
    public static ProductoMultimediaResponse fromEntity(ProductoMultimedia m) {
        return new ProductoMultimediaResponse(
            m.getIdMultimedia(),
            m.getTipo(),
            m.getUrl(),
            m.getNombreArchivo(),
            m.getEsPrincipal(),
            m.getFechaSubida()
        );
    }
}
