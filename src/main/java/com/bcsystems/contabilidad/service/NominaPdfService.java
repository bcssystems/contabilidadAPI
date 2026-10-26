package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Asistencia;
import com.bcsystems.contabilidad.domain.Nomina;
import com.bcsystems.contabilidad.domain.Usuario;
import com.bcsystems.contabilidad.domain.Comision;
import com.bcsystems.contabilidad.domain.Deduccion;
import com.bcsystems.contabilidad.repository.AsistenciaRepository;
import com.bcsystems.contabilidad.repository.ComisionRepository;
import com.bcsystems.contabilidad.repository.DeduccionRepository;
import com.bcsystems.contabilidad.repository.NominaRepository;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Service
public class NominaPdfService {
    private static final float WIDTH = 226.77f;
    private final NominaRepository nominaRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final ComisionRepository comisionRepository;
    private final DeduccionRepository deduccionRepository;

    public NominaPdfService(NominaRepository nominaRepository, AsistenciaRepository asistenciaRepository,
                            ComisionRepository comisionRepository, DeduccionRepository deduccionRepository) {
        this.nominaRepository = nominaRepository;
        this.asistenciaRepository = asistenciaRepository;
        this.comisionRepository = comisionRepository;
        this.deduccionRepository = deduccionRepository;
    }

    @Transactional
    public byte[] generar(Long id) {
        Nomina nomina = nominaRepository.findById(id).orElseThrow(() -> new NotFoundException("Nómina no encontrada"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PdfDocument pdf = new PdfDocument(new PdfWriter(output))) {
            Document document = new Document(pdf, new PageSize(WIDTH, 560));
             document.setMargins(5, 8, 5, 8);
            for (int copy = 0; copy < 2; copy++) {
                if (copy > 0) document.add(new AreaBreak(new PageSize(WIDTH, 560)));
                addReceipt(document, nomina);
            }
            document.close();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el recibo de nómina", e);
        }
        return output.toByteArray();
    }

    @Transactional
    public byte[] generarLote(List<Long> ids) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PdfDocument pdf = new PdfDocument(new PdfWriter(output))) {
            Document document = new Document(pdf, new PageSize(WIDTH, 560));
            document.setMargins(5, 8, 5, 8);
            boolean first = true;
            for (Long id : ids == null ? List.<Long>of() : ids) {
                Nomina nomina = nominaRepository.findById(id).orElseThrow(() -> new NotFoundException("Nómina no encontrada"));
                for (int copy = 0; copy < 2; copy++) {
                    if (!first) document.add(new AreaBreak(new PageSize(WIDTH, 560)));
                    addReceipt(document, nomina);
                    first = false;
                }
            }
            document.close();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el lote de recibos", e);
        }
        return output.toByteArray();
    }

    private void addReceipt(Document document, Nomina nomina) {
        Usuario user = nomina.getUsuario();
        String name = user != null && user.getPersona() != null
                ? String.join(" ", user.getPersona().getNombre(), user.getPersona().getPrimerApellido(),
                user.getPersona().getSegundoApellido() == null ? "" : user.getPersona().getSegundoApellido()).trim()
                : user == null ? "General" : user.getUsername();
        document.add(new Paragraph("NOMINA").setBold().setFontSize(14).setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("Copia idéntica").setFontSize(7).setTextAlignment(TextAlignment.CENTER));
        Table data = new Table(UnitValue.createPercentArray(new float[]{42, 58})).useAllAvailableWidth();
        row(data, "Nombre", name);
        row(data, "Puesto", user != null && user.getPosition() != null ? user.getPosition().name() : "-");
        row(data, "Fecha de pago", nomina.getFecha_nomina() == null ? "-" : nomina.getFecha_nomina().toString());
        row(data, "Periodo", periodo(nomina));
        row(data, "Horas laboradas", horas(user,
                nomina.getFechaInicio() != null ? nomina.getFechaInicio() : fechaPago(nomina).minusDays(6),
                nomina.getFechaFin() != null ? nomina.getFechaFin() : fechaPago(nomina)).toPlainString());
        row(data, "Sueldo base", money(nomina.getSueldoBase()));
        row(data, "Comisiones", money(nomina.getTotalComisiones()));
        row(data, "Deducciones", money(nomina.getTotalDeducciones()));
        row(data, "SALDO NETO", money(nomina.getSueldo()));
        document.add(data);

        document.add(new Paragraph("DESGLOSE DE PERCEPCIONES").setBold().setFontSize(8).setMarginTop(8));
        Table perceptions = new Table(UnitValue.createPercentArray(new float[]{72, 28})).useAllAvailableWidth();
        row(perceptions, "Sueldo base", money(nomina.getSueldoBase()));
        for (Comision commission : comisionRepository.findByNominaIdNomina(nomina.getIdNomina())) {
                String concept = commission.getConcepto() == null || commission.getConcepto().isBlank()
                        ? "Comisión" : commission.getConcepto();
                row(perceptions, concept, money(commission.getMontoComision()));
        }
        document.add(perceptions);

        document.add(new Paragraph("DESGLOSE DE DEDUCCIONES").setBold().setFontSize(8).setMarginTop(6));
        Table deductions = new Table(UnitValue.createPercentArray(new float[]{72, 28})).useAllAvailableWidth();
        var deductionsList = deduccionRepository.findByNominaIdNomina(nomina.getIdNomina());
        if (deductionsList.isEmpty()) {
            row(deductions, "Sin deducciones", money(BigDecimal.ZERO));
        } else {
            for (Deduccion deduction : deductionsList) {
                String date = deduction.getFecha() == null ? "sin fecha" : deduction.getFecha().toString();
                row(deductions, date + " - " + (deduction.getConcepto() == null ? "Deducción" : deduction.getConcepto()), money(deduction.getMonto()));
            }
        }
        document.add(deductions);
        document.add(new Paragraph("Firma del empleado: ____________________").setFontSize(8).setMarginTop(5));
        document.add(new Paragraph("Firma del responsable: __________________").setFontSize(8).setMarginTop(3));
    }

    private BigDecimal horas(Usuario user, LocalDate start, LocalDate end) {
        if (user == null || start == null || end == null) return BigDecimal.ZERO.setScale(2);
        long minutes = asistenciaRepository.findByUsuarioIdUserOrderByFechaDesc(user.getIdUser()).stream()
                .filter(a -> a.getFecha() != null && !a.getFecha().isBefore(start) && !a.getFecha().isAfter(end))
                .filter(a -> a.getEntrada() != null && a.getSalida() != null)
                .mapToLong(a -> Duration.between(a.getEntrada(), a.getSalida()).toMinutes()).sum();
        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private String money(BigDecimal amount) { return "$" + (amount == null ? BigDecimal.ZERO : amount).setScale(2, RoundingMode.HALF_UP); }

    private String periodo(Nomina nomina) {
        if (nomina.getFechaInicio() == null || nomina.getFechaFin() == null) return "-";
        return nomina.getFechaInicio() + " al " + nomina.getFechaFin();
    }

    private LocalDate fechaPago(Nomina nomina) {
        return nomina.getFecha_nomina() == null ? LocalDate.now() : nomina.getFecha_nomina();
    }

    private void row(Table table, String label, String value) {
        table.addCell(new Cell().setBorder(Border.NO_BORDER).add(new Paragraph(label).setBold().setFontSize(7)));
        table.addCell(new Cell().setBorder(Border.NO_BORDER).add(new Paragraph(value == null ? "-" : value).setFontSize(7)));
    }
}
