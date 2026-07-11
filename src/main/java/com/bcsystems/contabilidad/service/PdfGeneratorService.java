package com.bcsystems.contabilidad.service;

import com.bcsystems.contabilidad.domain.MovimientoAlmacen;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

public class PdfGeneratorService {

    private PdfGeneratorService() {}

    public static byte[] generarPdfSalida(MovimientoAlmacen movimiento) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("COMPROBANTE DE SALIDA DE ALMACEN")
                    .setBold()
                    .setFontSize(18)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
            document.add(new Paragraph(""));

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            Table table = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();

            addRow(table, "Fecha de salida:", movimiento.getFechaMovimiento() != null
                    ? movimiento.getFechaMovimiento().format(dtf) : "");
            addRow(table, "Folio de salida:", movimiento.getId().toString());
            addRow(table, "Medio de compra:", movimiento.getMedioCompra() != null ? movimiento.getMedioCompra() : "");
            addRow(table, "Descripcion:", movimiento.getDescripcion() != null ? movimiento.getDescripcion() : "");
            addRow(table, "Numero de serie:", movimiento.getVariante().getNumeroSerie());
            addRow(table, "IMEI:", movimiento.getImei() != null ? movimiento.getImei() : "N/A");
            addRow(table, "Precio de compra:", movimiento.getPrecioCompra() != null
                    ? "$" + movimiento.getPrecioCompra().toString() : "N/A");
            addRow(table, "Tipo de compra:", movimiento.getVariante().getTipoCompra() != null
                    ? movimiento.getVariante().getTipoCompra().name() : "N/A");
            addRow(table, "Proveedor:", movimiento.getProveedorNombre() != null ? movimiento.getProveedorNombre() : "N/A");
            addRow(table, "Vale foliado:", movimiento.getValeFolio() != null ? movimiento.getValeFolio() : "N/A");
            addRow(table, "Folio venta:", movimiento.getFolioVenta() != null ? movimiento.getFolioVenta() : "N/A");
            addRow(table, "Solicitante:", getNombreCompleto(movimiento.getSolicitante()));
            addRow(table, "Aprobador:", movimiento.getAprobador() != null
                    ? getNombreCompleto(movimiento.getAprobador()) : "Pendiente");
            addRow(table, "Observacion:", movimiento.getObservacion() != null ? movimiento.getObservacion() : "");

            document.add(table);
            document.close();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF de salida", e);
        }

        return baos.toByteArray();
    }

    private static void addRow(Table table, String label, String value) {
        table.addCell(new Paragraph(label).setBold());
        table.addCell(new Paragraph(value != null ? value : ""));
    }

    private static String getNombreCompleto(com.bcsystems.contabilidad.domain.Usuario usuario) {
        if (usuario.getPersona() != null) {
            return usuario.getPersona().getNombre() + " "
                    + usuario.getPersona().getPrimerApellido()
                    + (usuario.getPersona().getSegundoApellido() != null
                    ? " " + usuario.getPersona().getSegundoApellido() : "");
        }
        return usuario.getUsername();
    }
}
