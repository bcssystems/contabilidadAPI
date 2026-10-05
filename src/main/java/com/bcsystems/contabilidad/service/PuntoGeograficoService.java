package com.bcsystems.contabilidad.service;
import com.bcsystems.contabilidad.domain.PuntoGeografico;
import com.bcsystems.contabilidad.dto.PuntoGeograficoRequest;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.bcsystems.contabilidad.repository.PuntoGeograficoRepository;
import org.springframework.stereotype.Service;
import java.util.List;
@Service public class PuntoGeograficoService {
    private final PuntoGeograficoRepository repo;
    public PuntoGeograficoService(PuntoGeograficoRepository repo) { this.repo = repo; }
    public List<PuntoGeografico> all() { return repo.findAll(); }
    public PuntoGeografico save(PuntoGeograficoRequest r) { return repo.save(PuntoGeografico.builder().nombre(r.nombre()).latitud(r.latitud()).longitud(r.longitud()).radioMetros(r.radioMetros() == null ? 100 : r.radioMetros()).activo(r.activo() == null || r.activo()).build()); }
    public PuntoGeografico update(Integer id, PuntoGeograficoRequest r) { PuntoGeografico p = repo.findById(id).orElseThrow(() -> new NotFoundException("Punto no encontrado")); p.setNombre(r.nombre()); p.setLatitud(r.latitud()); p.setLongitud(r.longitud()); if (r.radioMetros() != null) p.setRadioMetros(r.radioMetros()); if (r.activo() != null) p.setActivo(r.activo()); return repo.save(p); }
    public void delete(Integer id) { PuntoGeografico p = repo.findById(id).orElseThrow(() -> new NotFoundException("Punto no encontrado")); p.setActivo(false); repo.save(p); }
}
