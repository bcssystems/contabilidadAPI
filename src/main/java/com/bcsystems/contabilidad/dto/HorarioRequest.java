package com.bcsystems.contabilidad.dto;
import com.bcsystems.contabilidad.domain.en.DiaSemana;
import java.time.LocalTime;
import java.util.List;
public record HorarioRequest(String nombre, List<DiaRequest> dias) {
    public record DiaRequest(DiaSemana dia, LocalTime entrada, LocalTime salida, List<BreakRequest> breaks) { }
    public record BreakRequest(LocalTime inicio, LocalTime fin) { }
}
