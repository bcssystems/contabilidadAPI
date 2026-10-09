package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.Asistencia;
import com.bcsystems.contabilidad.domain.Nomina;
import com.bcsystems.contabilidad.domain.Usuario;
import com.bcsystems.contabilidad.repository.AsistenciaRepository;
import com.bcsystems.contabilidad.repository.NominaRepository;
import com.bcsystems.contabilidad.exception.NotFoundException;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;

@Service
public class NominaPdfService {
    private static final float WIDTH = 226.77f;
    private final NominaRepository nominaRepository;
    private final AsistenciaRepository asistenciaRepository;

    public NominaPdfService(NominaRepository nominaRepository, AsistenciaRepository asistenciaRepository) {
        this.nominaRepository = nominaRepository;
        this.asistenciaRepository = asistenciaRepository;
    }

    @Transactional
    public byte[] generar(Long id) {
        Nomina nomina = nominaRepository.findById(id).orElseThrow(() -> new NotFoundException("Nómina no encontrada"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PdfDocument pdf = new PdfDocument(new PdfWriter(output))) {
            Document document = new Document(pdf, new PageSize(WIDTH, 560));
            document.setMargins(12, 10, 12, 10);
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
        row(data, "Horas laboradas", horas(user, nomina.getFecha_nomina()).toPlainString());
        row(data, "Sueldo base", money(nomina.getSueldoBase()));
        row(data, "Comisiones", money(nomina.getTotalComisiones()));
        row(data, "Deducciones", money(nomina.getTotalDeducciones()));
        row(data, "SALDO NETO", money(nomina.getSueldo()));
        document.add(data);
        document.add(new Paragraph("\n\nFirma del empleado: ____________________").setFontSize(8));
        document.add(new Paragraph(" ").setFontSize(4));
    }

    private BigDecimal horas(Usuario user, LocalDate paymentDate) {
        if (user == null || paymentDate == null) return BigDecimal.ZERO.setScale(2);
        long minutes = asistenciaRepository.findByUsuarioIdUserOrderByFechaDesc(user.getIdUser()).stream()
                .filter(a -> a.getFecha() != null && !a.getFecha().isBefore(paymentDate.minusDays(6)) && !a.getFecha().isAfter(paymentDate))
                .filter(a -> a.getEntrada() != null && a.getSalida() != null)
                .mapToLong(a -> Duration.between(a.getEntrada(), a.getSalida()).toMinutes()).sum();
        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private String money(BigDecimal amount) { return "$" + (amount == null ? BigDecimal.ZERO : amount).setScale(2, RoundingMode.HALF_UP); }

    private void row(Table table, String label, String value) {
        table.addCell(new Paragraph(label).setBold().setFontSize(7));
        table.addCell(new Paragraph(value == null ? "-" : value).setFontSize(7));
    }
}
