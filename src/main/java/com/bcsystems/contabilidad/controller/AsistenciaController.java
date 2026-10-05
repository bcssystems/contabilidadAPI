package com.bcsystems.contabilidad.controller;
import com.bcsystems.contabilidad.domain.en.TipoMarcaje;
import com.bcsystems.contabilidad.dto.*;
import com.bcsystems.contabilidad.service.AsistenciaService;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;
import java.math.BigDecimal;
@RestController @RequestMapping("/api/v1")
public class AsistenciaController {
    private final AsistenciaService service;
    public AsistenciaController(AsistenciaService service) { this.service=service; }
    @PostMapping(value="/checador/entrada", consumes=MediaType.MULTIPART_FORM_DATA_VALUE) public Object entrada(Authentication a,@RequestPart(value="selfie", required=false) MultipartFile selfie,@RequestParam BigDecimal latitud,@RequestParam BigDecimal longitud,@RequestParam(required=false) BigDecimal precisionMetros){return service.mark(a.getName(),TipoMarcaje.ENTRADA,selfie,latitud,longitud,precisionMetros);}
    @PostMapping(value="/checador/salida", consumes=MediaType.MULTIPART_FORM_DATA_VALUE) public Object salida(Authentication a,@RequestPart(value="selfie", required=false) MultipartFile selfie,@RequestParam BigDecimal latitud,@RequestParam BigDecimal longitud,@RequestParam(required=false) BigDecimal precisionMetros){return service.mark(a.getName(),TipoMarcaje.SALIDA,selfie,latitud,longitud,precisionMetros);}
    @GetMapping("/checador/mi-tablero") public Object tablero(Authentication a){return service.dashboard(a.getName());}
    @GetMapping("/checador/mi-horario") public Object schedule(Authentication a){return service.mySchedule(a.getName());}
    @GetMapping("/asistencias") @PreAuthorize("hasRole('ADMIN')") public Object all(){return service.all();}
    @GetMapping("/asistencias/{id}") @PreAuthorize("hasRole('ADMIN')") public Object one(@PathVariable Integer id){return service.one(id);}
    @GetMapping("/asistencias/evidencias/pendientes") @PreAuthorize("hasRole('ADMIN')") public Object pendingEvidence(){return service.pendingEvidence();}
    @PostMapping("/asistencias/evidencias/{id}/aprobar") @PreAuthorize("hasRole('ADMIN')") public Object approveEvidence(@PathVariable Integer id){return service.reviewEvidence(id,true);}
    @PostMapping("/asistencias/evidencias/{id}/rechazar") @PreAuthorize("hasRole('ADMIN')") public Object rejectEvidence(@PathVariable Integer id){return service.reviewEvidence(id,false);}
    @PostMapping("/justificaciones") public Object justification(Authentication a,@RequestBody JustificacionRequest r){return service.justification(a.getName(),r.asistenciaId(),r.comentario(),null);}
    @PostMapping(value="/justificaciones/multipart", consumes=MediaType.MULTIPART_FORM_DATA_VALUE) public Object justificationFile(Authentication a,@RequestParam Integer asistenciaId,@RequestParam String comentario,@RequestPart(required=false) MultipartFile archivo){return service.justification(a.getName(),asistenciaId,comentario,archivo);}
    @GetMapping("/justificaciones/mias") public Object myJustifications(Authentication a){return service.myJustifications(a.getName());}
    @GetMapping("/justificaciones") @PreAuthorize("hasRole('ADMIN')") public Object allPendingJustifications(){return service.pendingJustifications();}
    @GetMapping("/justificaciones/pendientes") @PreAuthorize("hasRole('ADMIN')") public Object pendingJustifications(){return service.pendingJustifications();}
    @PostMapping("/justificaciones/{id}/aprobar") @PreAuthorize("hasRole('ADMIN')") public Object approveJustification(@PathVariable Integer id){return service.reviewJustification(id,true);}
    @PostMapping("/justificaciones/{id}/rechazar") @PreAuthorize("hasRole('ADMIN')") public Object rejectJustification(@PathVariable Integer id){return service.reviewJustification(id,false);}
    @PostMapping("/nomina/asistencia/generar") @PreAuthorize("hasRole('ADMIN')") public Object generatePayrollDeductions(@RequestBody GenerarDeduccionesAsistenciaRequest request){return service.generateAttendanceDeductions(request.usuarioId(),request.inicio(),request.fin());}
}
