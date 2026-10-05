package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.*;
import com.bcsystems.contabilidad.dto.*;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.Duration;
import java.math.BigDecimal;
import java.util.List;

@Service
public class HorarioService {
    private final HorarioRepository horarios;
    private final UsuarioRepository usuarios;
    private final UsuarioHorarioRepository asignaciones;
    public HorarioService(HorarioRepository horarios, UsuarioRepository usuarios, UsuarioHorarioRepository asignaciones) {
        this.horarios = horarios; this.usuarios = usuarios; this.asignaciones = asignaciones;
    }
    public List<Horario> findAll() { return horarios.findAll(); }
    public Horario find(Integer id) { return horarios.findById(id).orElseThrow(() -> new NotFoundException("Horario no encontrado")); }
    @Transactional public Horario save(HorarioRequest r) {
        Horario h = Horario.builder().nombre(r.nombre()).activo(true).build();
        fill(h, r); return horarios.save(h);
    }
    @Transactional public Horario update(Integer id, HorarioRequest r) { Horario h = find(id); h.setNombre(r.nombre()); h.getDias().clear(); fill(h, r); return horarios.save(h); }
    public void delete(Integer id) { Horario h = find(id); h.setActivo(false); horarios.save(h); }
    private void fill(Horario h, HorarioRequest r) {
        if (r.dias() == null) return;
        for (HorarioRequest.DiaRequest d : r.dias()) {
            if (d.entrada() == null || d.salida() == null || !d.entrada().isBefore(d.salida())) throw new IllegalArgumentException("Los horarios no pueden cruzar medianoche");
            HorarioDia hd = HorarioDia.builder().horario(h).dia(d.dia()).entrada(d.entrada()).salida(d.salida()).build();
            if (d.breaks() != null) for (HorarioRequest.BreakRequest b : d.breaks()) {
                if (b.inicio() == null || b.fin() == null || !b.inicio().isBefore(b.fin())) throw new IllegalArgumentException("Break invalido");
                hd.getBreaks().add(HorarioBreak.builder().horarioDia(hd).inicio(b.inicio()).fin(b.fin()).build());
            }
            h.getDias().add(hd);
        }
    }
    @Transactional public UsuarioHorario assign(Integer userId, AsignarHorarioRequest r) {
        Usuario user = usuarios.findById(userId).orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        Horario h = find(r.horarioId());
        asignaciones.findFirstByUsuarioIdUserAndActivoTrueOrderByIdDesc(userId).ifPresent(a -> { a.setActivo(false); asignaciones.save(a); });
        return asignaciones.save(UsuarioHorario.builder().usuario(user).horario(h).vigenteDesde(r.vigenteDesde() == null ? LocalDate.now() : r.vigenteDesde()).vigenteHasta(r.vigenteHasta()).activo(true).build());
    }
    public UsuarioHorario assigned(Integer userId) { return asignaciones.findFirstByUsuarioIdUserAndActivoTrueOrderByIdDesc(userId).orElseThrow(() -> new NotFoundException("Horario no asignado")); }
    public BigDecimal weeklyHours(Horario horario) {
        long minutes = horario.getDias().stream().mapToLong(d -> {
            long total = Duration.between(d.getEntrada(), d.getSalida()).toMinutes();
            return total - d.getBreaks().stream().mapToLong(b -> Duration.between(b.getInicio(), b.getFin()).toMinutes()).sum();
        }).sum();
        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
    }
}
