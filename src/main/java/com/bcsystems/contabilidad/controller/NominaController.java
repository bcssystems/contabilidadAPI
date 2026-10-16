package com.bcsystems.contabilidad.controller;

import com.bcsystems.contabilidad.dto.NominaRegisterRequest;
import com.bcsystems.contabilidad.dto.NominaResponse;
import com.bcsystems.contabilidad.dto.NominaUpdateRequest;
import com.bcsystems.contabilidad.service.NominaService;
import com.bcsystems.contabilidad.service.NominaPdfService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.bcsystems.contabilidad.domain.en.EstadoNomina;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/nomina")
public class NominaController {

    private final NominaService nominaService;
    private final NominaPdfService nominaPdfService;

    public NominaController(NominaService nominaService, NominaPdfService nominaPdfService) {
        this.nominaService = nominaService;
        this.nominaPdfService = nominaPdfService;
    }

    @PostMapping
    public ResponseEntity<?> save(@RequestBody NominaRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(nominaService.save(request));
    }

    @GetMapping
    public ResponseEntity<?> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "idNomina") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) EstadoNomina estado,
            @RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending() 
            : Sort.by(sortBy).ascending();
        
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<NominaResponse> result = nominaService.findFiltered(pageRequest, search, estado, desde, hasta);
        
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/desglose/{usuarioId}")
    public ResponseEntity<?> desglose(@PathVariable Integer usuarioId,
                                      @RequestParam(required = false) LocalDate inicio,
                                      @RequestParam(required = false) LocalDate fin) {
        return ResponseEntity.status(HttpStatus.OK).body(nominaService.desglose(usuarioId, inicio, fin));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(nominaService.findById(id));
    }

    @GetMapping("/{id}/desglose")
    public ResponseEntity<?> desgloseNomina(@PathVariable Long id) {
        return ResponseEntity.ok(nominaService.desgloseNomina(id));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) {
        return ResponseEntity.ok().header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=nomina-" + id + ".pdf")
                .body(nominaPdfService.generar(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody NominaUpdateRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(nominaService.update(id, request));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelar(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(nominaService.cancelar(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        nominaService.delete(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
