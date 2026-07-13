package com.bcsystems.contabilidad.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ProductoEcommerceResponse(
        @JsonProperty("id") Integer id,
        @JsonProperty("nombre") String nombre,
        @JsonProperty("marca") String marca,
        @JsonProperty("modelo") String modelo,
        @JsonProperty("precioVenta") Double precioVenta,
        @JsonProperty("estado") String estado,
        @JsonProperty("categoria") String categoria,
        @JsonProperty("imagenes") List<String> imagenes,
        @JsonProperty("descripcion") String descripcion,
        @JsonProperty("especificaciones") List<String> especificaciones,
        @JsonProperty("mensajeWhatsapp") String mensajeWhatsapp
) {}
