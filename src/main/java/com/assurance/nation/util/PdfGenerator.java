package com.assurance.nation.util;

import com.assurance.nation.entity.Reimbursement;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Génère un justificatif PDF pour les remboursements (OpenPDF).
 */
@Component
public class PdfGenerator {

    @Value("${app.pdf.output-dir:./data/justificatifs}")
    private String outputDir;

    public String generateJustificatif(Reimbursement reimbursement) throws IOException {
        Path dir = Paths.get(outputDir);
        Files.createDirectories(dir);
        String fileName = reimbursement.getNumRemboursement() + ".pdf";
        Path file = dir.resolve(fileName);

        Document document = new Document();
        try {
            PdfWriter.getInstance(document, Files.newOutputStream(file));
            document.open();
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            document.add(new Paragraph("ASSURANCE NATION", titleFont));
            document.add(new Paragraph("Justificatif de remboursement", bodyFont));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Numéro : " + reimbursement.getNumRemboursement(), bodyFont));
            document.add(new Paragraph("Montant total : " + reimbursement.getMontantTotal() + " €", bodyFont));
            document.add(new Paragraph("Taux : " + reimbursement.getTauxRemboursement() + " %", bodyFont));
            document.add(new Paragraph("Montant remboursé : " + reimbursement.getMontantRembourse() + " €", bodyFont));
            document.add(new Paragraph("Statut : " + reimbursement.getStatus(), bodyFont));
            if (reimbursement.getModePaiement() != null) {
                document.add(new Paragraph("Mode de paiement : " + reimbursement.getModePaiement(), bodyFont));
            }
        } finally {
            document.close();
        }
        return file.toAbsolutePath().toString();
    }
}
