package com.example.DeviSmart.services;

import com.example.DeviSmart.entities.Facture;
import com.example.DeviSmart.entities.LigneFacture;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

/**
 * Service de génération PDF pour les factures
 * 
 * Utilise les données intégrées (dénormalisées) de l'entité Facture,
 * garantissant l'immutabilité des documents légaux.
 */
@Service
public class FacturePdfService {

    // Couleurs du thème
    private static final Color PRIMARY_COLOR = new Color(220, 38, 38);    // #dc2626 - Rouge
    private static final Color DARK_COLOR = new Color(15, 23, 42);        // #0f172a
    private static final Color GRAY_COLOR = new Color(100, 116, 139);     // #64748b
    private static final Color LIGHT_GRAY = new Color(241, 245, 249);     // #f1f5f9
    private static final Color BORDER_COLOR = new Color(226, 232, 240);   // #e2e8f0

    // Formatage
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.000");
    private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("0.00");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Polices
    private Font titleFont;
    private Font headerFont;
    private Font labelFont;
    private Font valueFont;
    private Font dataFont;
    private Font totalFont;
    private Font footerFont;

    public FacturePdfService() {
        initFonts();
    }

    private void initFonts() {
        titleFont = new Font(Font.HELVETICA, 24, Font.BOLD, PRIMARY_COLOR);
        headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
        labelFont = new Font(Font.HELVETICA, 10, Font.NORMAL, GRAY_COLOR);
        valueFont = new Font(Font.HELVETICA, 10, Font.BOLD, DARK_COLOR);
        dataFont = new Font(Font.HELVETICA, 10, Font.NORMAL, DARK_COLOR);
        totalFont = new Font(Font.HELVETICA, 14, Font.BOLD, PRIMARY_COLOR);
        footerFont = new Font(Font.HELVETICA, 8, Font.NORMAL, GRAY_COLOR);
    }

    /**
     * Génère le PDF d'une facture
     * 
     * @param facture Facture avec données intégrées
     * @return Bytes du PDF généré
     */
    public byte[] generer(Facture facture) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, baos);

            document.open();

            // 1. En-tête entreprise (données intégrées)
            ajouterEntete(document, facture);

            // 2. Titre et numéro
            ajouterTitre(document, facture);

            // 3. Infos facture
            ajouterInfosFacture(document, facture);

            // 4. Bloc client
            ajouterBlocClient(document, facture);

            // 5. Tableau des lignes
            ajouterTableauLignes(document, facture);

            // 6. Totaux
            ajouterTotaux(document, facture);

            // 7. Conditions et mentions
            ajouterConditions(document, facture);

            // 8. Pied de page
            ajouterPiedDePage(document, facture);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erreur génération PDF facture: " + e.getMessage(), e);
        }
    }

    // ==================== SECTIONS DU PDF ====================

    private void ajouterEntete(Document document, Facture facture) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);

        // Nom entreprise
        PdfPCell nomCell = new PdfPCell(new Phrase(facture.getNomEntreprise(), titleFont));
        nomCell.setBorder(Rectangle.NO_BORDER);
        nomCell.setPaddingBottom(10);
        table.addCell(nomCell);

        // Coordonnées entreprise
        StringBuilder coords = new StringBuilder();
        if (facture.getAdresseEntreprise() != null) {
            coords.append(facture.getAdresseEntreprise()).append("\n");
        }
        if (facture.getTelephoneEntreprise() != null) {
            coords.append("Tél: ").append(facture.getTelephoneEntreprise()).append("  ");
        }
        if (facture.getEmailEntreprise() != null) {
            coords.append("Email: ").append(facture.getEmailEntreprise()).append("\n");
        }
        if (facture.getMatriculeFiscal() != null) {
            coords.append("Matricule Fiscal: ").append(facture.getMatriculeFiscal());
        }
        if (facture.getRegistreCommerce() != null) {
            coords.append("  |  RC: ").append(facture.getRegistreCommerce());
        }

        PdfPCell coordsCell = new PdfPCell(new Phrase(coords.toString(), labelFont));
        coordsCell.setBorder(Rectangle.NO_BORDER);
        coordsCell.setPaddingBottom(15);
        table.addCell(coordsCell);

        document.add(table);

        // Ligne de séparation
        ajouterSeparateur(document);
    }

    private void ajouterTitre(Document document, Facture facture) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(15);

        // Titre "FACTURE"
        Font bigTitleFont = new Font(Font.HELVETICA, 28, Font.BOLD, DARK_COLOR);
        PdfPCell titleCell = new PdfPCell(new Phrase("FACTURE", bigTitleFont));
        titleCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(titleCell);

        // Numéro
        Font numFont = new Font(Font.HELVETICA, 14, Font.BOLD, PRIMARY_COLOR);
        Paragraph numPara = new Paragraph("N° " + facture.getNumeroFacture(), numFont);
        numPara.setAlignment(Element.ALIGN_RIGHT);
        PdfPCell numCell = new PdfPCell(numPara);
        numCell.setBorder(Rectangle.NO_BORDER);
        numCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        numCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(numCell);

        document.add(table);
    }

    private void ajouterInfosFacture(Document document, Facture facture) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(50);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.setSpacingBefore(15);

        // Date d'émission
        ajouterLigneInfo(table, "Date d'émission:", facture.getDateEmission().format(DATE_FORMAT));

        // Date d'échéance
        if (facture.getDateEcheance() != null) {
            ajouterLigneInfo(table, "Date d'échéance:", facture.getDateEcheance().format(DATE_FORMAT));
        }

        // Référence devis
        if (facture.getReferenceDevis() != null && !facture.getReferenceDevis().isEmpty()) {
            ajouterLigneInfo(table, "Réf. devis:", facture.getReferenceDevis());
        }

        // Statut avec couleur
        ajouterLigneInfo(table, "Statut:", formatStatut(facture.getStatut()));

        document.add(table);
    }

    private void ajouterBlocClient(Document document, Facture facture) throws DocumentException {
        document.add(new Paragraph("\n"));

        // Titre section
        Font sectionFont = new Font(Font.HELVETICA, 12, Font.BOLD, DARK_COLOR);
        document.add(new Paragraph("CLIENT", sectionFont));

        // Cadre client
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(50);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.setSpacingBefore(5);

        // Nom client
        Font clientNameFont = new Font(Font.HELVETICA, 11, Font.BOLD, DARK_COLOR);
        PdfPCell nameCell = new PdfPCell(new Phrase(facture.getNomClient(), clientNameFont));
        nameCell.setBackgroundColor(LIGHT_GRAY);
        nameCell.setPadding(10);
        nameCell.setBorderColor(BORDER_COLOR);
        table.addCell(nameCell);

        // Détails client
        StringBuilder details = new StringBuilder();
        if (facture.getAdresseClient() != null) {
            details.append(facture.getAdresseClient()).append("\n");
        }
        if (facture.getTelephoneClient() != null) {
            details.append("Tél: ").append(facture.getTelephoneClient()).append("\n");
        }
        if (facture.getEmailClient() != null) {
            details.append("Email: ").append(facture.getEmailClient()).append("\n");
        }
        if (facture.getMatriculeFiscalClient() != null) {
            details.append("MF: ").append(facture.getMatriculeFiscalClient());
        }

        PdfPCell detailsCell = new PdfPCell(new Phrase(details.toString(), dataFont));
        detailsCell.setBackgroundColor(LIGHT_GRAY);
        detailsCell.setPadding(10);
        detailsCell.setPaddingTop(0);
        detailsCell.setBorderColor(BORDER_COLOR);
        table.addCell(detailsCell);

        document.add(table);
    }

    private void ajouterTableauLignes(Document document, Facture facture) throws DocumentException {
        document.add(new Paragraph("\n\n"));

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{4, 1, 1.5f, 1.5f});

        // En-têtes
        String[] headers = {"DÉSIGNATION", "QTÉ", "P.U. HT", "TOTAL HT"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(DARK_COLOR);
            cell.setPadding(10);
            cell.setHorizontalAlignment(header.equals("DÉSIGNATION") ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT);
            table.addCell(cell);
        }

        // Lignes de données
        boolean alternate = false;
        if (facture.getLignes() != null) {
            for (LigneFacture ligne : facture.getLignes()) {
                Color bgColor = alternate ? LIGHT_GRAY : Color.WHITE;

                // Désignation
                PdfPCell cell = new PdfPCell(new Phrase(ligne.getDesignation(), dataFont));
                cell.setBackgroundColor(bgColor);
                cell.setPadding(8);
                cell.setBorderColor(BORDER_COLOR);
                table.addCell(cell);

                // Quantité
                cell = new PdfPCell(new Phrase(formatDecimal(ligne.getQuantite()), dataFont));
                cell.setBackgroundColor(bgColor);
                cell.setPadding(8);
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cell.setBorderColor(BORDER_COLOR);
                table.addCell(cell);

                // Prix unitaire
                cell = new PdfPCell(new Phrase(formatMontant(ligne.getPrixUnitaireHT()), dataFont));
                cell.setBackgroundColor(bgColor);
                cell.setPadding(8);
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cell.setBorderColor(BORDER_COLOR);
                table.addCell(cell);

                // Total ligne
                cell = new PdfPCell(new Phrase(formatMontant(ligne.getTotalLigneHT()), dataFont));
                cell.setBackgroundColor(bgColor);
                cell.setPadding(8);
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cell.setBorderColor(BORDER_COLOR);
                table.addCell(cell);

                alternate = !alternate;
            }
        }

        document.add(table);
    }

    private void ajouterTotaux(Document document, Facture facture) throws DocumentException {
        document.add(new Paragraph("\n"));

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(40);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);

        // Total HT
        ajouterLigneTotaux(table, "Total HT", formatMontant(facture.getTotalHT()), false);

        // TVA
        String tauxLabel = "TVA (" + PERCENT_FORMAT.format(facture.getTauxTVAPourcentage()) + "%)";
        ajouterLigneTotaux(table, tauxLabel, formatMontant(facture.getMontantTVA()), false);

        // Total TTC (mis en évidence)
        PdfPCell labelCell = new PdfPCell(new Phrase("TOTAL TTC", totalFont));
        labelCell.setBackgroundColor(LIGHT_GRAY);
        labelCell.setPadding(12);
        labelCell.setBorderColor(PRIMARY_COLOR);
        labelCell.setBorderWidth(2);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(formatMontant(facture.getTotalTTC()), totalFont));
        valueCell.setBackgroundColor(LIGHT_GRAY);
        valueCell.setPadding(12);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setBorderColor(PRIMARY_COLOR);
        valueCell.setBorderWidth(2);
        table.addCell(valueCell);

        document.add(table);
    }

    private void ajouterConditions(Document document, Facture facture) throws DocumentException {
        document.add(new Paragraph("\n\n"));

        if (facture.getConditionsPaiement() != null && !facture.getConditionsPaiement().isEmpty()) {
            Font condTitleFont = new Font(Font.HELVETICA, 11, Font.BOLD, DARK_COLOR);
            document.add(new Paragraph("CONDITIONS DE PAIEMENT", condTitleFont));

            Paragraph condPara = new Paragraph(facture.getConditionsPaiement(), labelFont);
            condPara.setSpacingBefore(5);
            document.add(condPara);
        }

        if (facture.getNotes() != null && !facture.getNotes().isEmpty()) {
            Font notesTitleFont = new Font(Font.HELVETICA, 11, Font.BOLD, DARK_COLOR);
            Paragraph notesTitre = new Paragraph("NOTES", notesTitleFont);
            notesTitre.setSpacingBefore(10);
            document.add(notesTitre);

            Paragraph notesPara = new Paragraph(facture.getNotes(), labelFont);
            notesPara.setSpacingBefore(5);
            document.add(notesPara);
        }
    }

    private void ajouterPiedDePage(Document document, Facture facture) throws DocumentException {
        document.add(new Paragraph("\n\n"));

        Paragraph footer = new Paragraph();
        footer.setAlignment(Element.ALIGN_CENTER);

        StringBuilder mentions = new StringBuilder();
        mentions.append(facture.getNomEntreprise());
        if (facture.getMatriculeFiscal() != null) {
            mentions.append(" - MF: ").append(facture.getMatriculeFiscal());
        }
        if (facture.getAdresseEntreprise() != null) {
            mentions.append("\n").append(facture.getAdresseEntreprise());
        }
        mentions.append("\nDocument généré par DeviSmart");

        footer.add(new Chunk(mentions.toString(), footerFont));
        document.add(footer);
    }

    // ==================== UTILITAIRES ====================

    private void ajouterSeparateur(Document document) throws DocumentException {
        PdfPTable sep = new PdfPTable(1);
        sep.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell();
        cell.setBorderColor(PRIMARY_COLOR);
        cell.setBorderWidth(2);
        cell.setBorder(Rectangle.BOTTOM);
        cell.setPaddingBottom(5);
        sep.addCell(cell);
        document.add(sep);
    }

    private void ajouterLigneInfo(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingBottom(5);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPaddingBottom(5);
        table.addCell(valueCell);
    }

    private void ajouterLigneTotaux(PdfPTable table, String label, String value, boolean highlight) {
        Font lFont = highlight ? totalFont : valueFont;
        Font vFont = highlight ? totalFont : valueFont;

        PdfPCell labelCell = new PdfPCell(new Phrase(label, lFont));
        labelCell.setPadding(8);
        labelCell.setBorderColor(BORDER_COLOR);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, vFont));
        valueCell.setPadding(8);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setBorderColor(BORDER_COLOR);
        table.addCell(valueCell);
    }

    private String formatMontant(BigDecimal montant) {
        if (montant == null) return "0,000 TND";
        return DECIMAL_FORMAT.format(montant) + " TND";
    }

    private String formatDecimal(BigDecimal value) {
        if (value == null) return "0";
        return value.stripTrailingZeros().toPlainString();
    }

    private String formatStatut(String statut) {
        if (statut == null) return "N/A";
        switch (statut) {
            case "PAYEE": return "✓ Payée";
            case "NON_PAYEE": return "⏳ Non payée";
            case "EN_RETARD": return "⚠ En retard";
            case "ANNULEE": return "✗ Annulée";
            default: return statut;
        }
    }
}
