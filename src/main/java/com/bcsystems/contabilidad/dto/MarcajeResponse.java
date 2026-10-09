package com.bcsystems.contabilidad.dto;
import com.bcsystems.contabilidad.domain.en.TipoMarcaje;
import java.time.LocalDateTime;
public record MarcajeResponse(Integer asistenciaId, Integer marcajeId, TipoMarcaje tipo, LocalDateTime marcadoEn,
                              LocalDateTime entrada, LocalDateTime salida, Double distanciaMetros,
                               String estadoUbicacion, String estadoAsistencia, boolean pendiente, String puntoNombre,
                               Double latitud, Double longitud, Integer radioPermitido) { }
