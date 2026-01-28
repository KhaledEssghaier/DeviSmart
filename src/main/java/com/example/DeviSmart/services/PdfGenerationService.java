package com.example.DeviSmart.services;

import com.example.DeviSmart.entities.*;
import com.example.DeviSmart.repositories.EntrepriseRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

@Service
public class PdfGenerationService {

    @Autowired
    private EntrepriseRepository entrepriseRepository;

    private static final Color PRIMARY_COLOR = new Color(220, 38, 38); // #dc2626
    private static final Color DARK_COLOR = new Color(15, 23, 42); // #0f172a
    private static final Color GRAY_COLOR = new Color(100, 116, 139); // #64748b
    private static final Color LIGHT_GRAY = new Color(241, 245, 249); // #f1f5f9
    
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.000");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired
    private FacturePdfService facturePdfService;

    /**
     * Génère un PDF pour une facture (utilise le nouveau service)
     */
    public byte[] genererPdfFacture(Facture facture) {
        return facturePdfService.generer(facture);
    }

    /**
     * Génère un PDF pour un devis
     */
    public byte[] genererPdfDevis(Devis devis) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, baos);
            
            document.open();
            
            Entreprise entreprise = entrepriseRepository.getEntreprise();
            
            // En-tête avec infos entreprise
            ajouterEnTeteEntreprise(document, entreprise);
            
            // Titre du document
            ajouterTitreDocument(document, "DEVIS", devis.getNumero());
            
            // Infos devis
            ajouterInfosDevis(document, devis);
            
            // Infos client
            ajouterInfosClient(document, devis.getClient());
            
            // Tableau des lignes
            ajouterTableauLignes(document, devis.getLignes());
            
            // Totaux
            ajouterTotaux(document, devis.getTotalHT(), devis.getTauxTVA(), 
                         devis.getMontantTVA(), devis.getTotalTTC());
            
            // Conditions de validité
            ajouterConditionsDevis(document, devis);
            
            // Mentions légales
            ajouterMentionsLegales(document, entreprise);
            
            document.close();
            
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF de devis", e);
        }
    }

    private void ajouterEnTeteEntreprise(Document document, Entreprise entreprise) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);
        
        // Nom de l'entreprise
        Font titleFont = new Font(Font.HELVETICA, 24, Font.BOLD, PRIMARY_COLOR);
        Paragraph nomEntreprise = new Paragraph(
            entreprise != null ? entreprise.getNom() : "DeviSmart", titleFont);
        
        PdfPCell cell = new PdfPCell(nomEntreprise);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingBottom(10);
        headerTable.addCell(cell);
        
        // Coordonnées
        if (entreprise != null) {
            Font infoFont = new Font(Font.HELVETICA, 10, Font.NORMAL, GRAY_COLOR);
            StringBuilder infos = new StringBuilder();
            
            if (entreprise.getAdresse() != null) {
                infos.append(entreprise.getAdresseComplete()).append("\n");
            }
            if (entreprise.getTelephone() != null) {
                infos.append("Tél: ").append(entreprise.getTelephone()).append("  ");
            }
            if (entreprise.getEmail() != null) {
                infos.append("Email: ").append(entreprise.getEmail()).append("\n");
            }
            if (entreprise.getMatriculeFiscal() != null) {
                infos.append("Matricule Fiscal: ").append(entreprise.getMatriculeFiscal());
            }
            
            Paragraph coordonnees = new Paragraph(infos.toString(), infoFont);
            cell = new PdfPCell(coordonnees);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setPaddingBottom(20);
            headerTable.addCell(cell);
        }
        
        document.add(headerTable);
        
        // Ligne de séparation
        PdfPTable separateur = new PdfPTable(1);
        separateur.setWidthPercentage(100);
        PdfPCell sepCell = new PdfPCell();
        sepCell.setBorderColor(PRIMARY_COLOR);
        sepCell.setBorderWidth(2);
        sepCell.setBorder(Rectangle.BOTTOM);
        sepCell.setPaddingBottom(10);
        separateur.addCell(sepCell);
        document.add(separateur);
        
        document.add(new Paragraph("\n"));
    }

    private void ajouterTitreDocument(Document document, String type, String numero) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 1});
        
        // Type de document
        Font typeFont = new Font(Font.HELVETICA, 28, Font.BOLD, DARK_COLOR);
        Paragraph typePara = new Paragraph(type, typeFont);
        PdfPCell typeCell = new PdfPCell(typePara);
        typeCell.setBorder(Rectangle.NO_BORDER);
        typeCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(typeCell);
        
        // Numéro
        Font numFont = new Font(Font.HELVETICA, 14, Font.BOLD, PRIMARY_COLOR);
        Paragraph numPara = new Paragraph("N° " + numero, numFont);
        numPara.setAlignment(Element.ALIGN_RIGHT);
        PdfPCell numCell = new PdfPCell(numPara);
        numCell.setBorder(Rectangle.NO_BORDER);
        numCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        numCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(numCell);
        
        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void ajouterInfosDocument(Document document, Facture facture) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(50);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        
        Font labelFont = new Font(Font.HELVETICA, 10, Font.NORMAL, GRAY_COLOR);
        Font valueFont = new Font(Font.HELVETICA, 10, Font.BOLD, DARK_COLOR);
        
        // Date d'émission
        ajouterLigneInfo(table, "Date d'émission:", 
            facture.getDateEmission().format(DATE_FORMATTER), labelFont, valueFont);
        
        // Référence devis
        if (facture.getReferenceDevis() != null && !facture.getReferenceDevis().isEmpty()) {
            ajouterLigneInfo(table, "Référence devis:", 
                facture.getReferenceDevis(), labelFont, valueFont);
        }
        
        // Statut
        ajouterLigneInfo(table, "Statut:", facture.getStatut(), labelFont, valueFont);
        
        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void ajouterInfosDevis(Document document, Devis devis) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(50);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        
        Font labelFont = new Font(Font.HELVETICA, 10, Font.NORMAL, GRAY_COLOR);
        Font valueFont = new Font(Font.HELVETICA, 10, Font.BOLD, DARK_COLOR);
        
        // Date de création
        ajouterLigneInfo(table, "Date de création:", 
            devis.getDateCreation().format(DATE_FORMATTER), labelFont, valueFont);
        
        // Date de validité
        if (devis.getDateValidite() != null) {
            ajouterLigneInfo(table, "Valable jusqu'au:", 
                devis.getDateValidite().format(DATE_FORMATTER), labelFont, valueFont);
        }
        
        // Statut
        ajouterLigneInfo(table, "Statut:", devis.getStatut(), labelFont, valueFont);
        
        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void ajouterLigneInfo(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingBottom(5);
        table.addCell(labelCell);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPaddingBottom(5);
        table.addCell(valueCell);
    }

    private void ajouterInfosClient(Document document, Client client) throws DocumentException {
        // Titre section
        Font sectionFont = new Font(Font.HELVETICA, 12, Font.BOLD, DARK_COLOR);
        Paragraph titre = new Paragraph("CLIENT", sectionFont);
        document.add(titre);
        
        // Cadre client
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(50);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        
        Font clientFont = new Font(Font.HELVETICA, 11, Font.NORMAL, DARK_COLOR);
        Font clientBoldFont = new Font(Font.HELVETICA, 11, Font.BOLD, DARK_COLOR);
        
        StringBuilder clientInfos = new StringBuilder();
        if (client != null) {
            PdfPCell nameCell = new PdfPCell(new Phrase(client.getNom(), clientBoldFont));
            nameCell.setBackgroundColor(LIGHT_GRAY);
            nameCell.setPadding(10);
            nameCell.setBorderColor(new Color(226, 232, 240));
            table.addCell(nameCell);
            
            StringBuilder details = new StringBuilder();
            if (client.getAdresse() != null) {
                details.append(client.getAdresse()).append("\n");
            }
            if (client.getTelephone() != null) {
                details.append("Tél: ").append(client.getTelephone()).append("\n");
            }
            if (client.getEmail() != null) {
                details.append("Email: ").append(client.getEmail()).append("\n");
            }
            if (client.getMatriculeFiscal() != null) {
                details.append("MF: ").append(client.getMatriculeFiscal());
            }
            
            PdfPCell detailsCell = new PdfPCell(new Phrase(details.toString(), clientFont));
            detailsCell.setBackgroundColor(LIGHT_GRAY);
            detailsCell.setPadding(10);
            detailsCell.setPaddingTop(0);
            detailsCell.setBorderColor(new Color(226, 232, 240));
            table.addCell(detailsCell);
        }
        
        document.add(table);
        document.add(new Paragraph("\n\n"));
    }

    private void ajouterTableauLignes(Document document, java.util.List<Ligne> lignes) throws DocumentException {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 1, 1.5f, 1.5f, 1.5f});
        
        // En-têtes
        Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
        String[] headers = {"DÉSIGNATION", "QTÉ", "P.U. HT", "TOTAL HT", ""};
        
        for (int i = 0; i < 4; i++) {
            PdfPCell cell = new PdfPCell(new Phrase(headers[i], headerFont));
            cell.setBackgroundColor(DARK_COLOR);
            cell.setPadding(10);
            cell.setHorizontalAlignment(i == 0 ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT);
            table.addCell(cell);
        }
        
        // Dernière colonne vide pour l'en-tête
        PdfPCell emptyHeader = new PdfPCell(new Phrase("", headerFont));
        emptyHeader.setBackgroundColor(DARK_COLOR);
        emptyHeader.setPadding(10);
        table.addCell(emptyHeader);
        
        // Lignes de données
        Font dataFont = new Font(Font.HELVETICA, 10, Font.NORMAL, DARK_COLOR);
        boolean alternate = false;
        
        if (lignes != null) {
            for (Ligne ligne : lignes) {
                Color bgColor = alternate ? LIGHT_GRAY : Color.WHITE;
                
                // Désignation
                PdfPCell cell = new PdfPCell(new Phrase(ligne.getDesignation(), dataFont));
                cell.setBackgroundColor(bgColor);
                cell.setPadding(8);
                table.addCell(cell);
                
                // Quantité
                cell = new PdfPCell(new Phrase(String.valueOf(ligne.getQuantite()), dataFont));
                cell.setBackgroundColor(bgColor);
                cell.setPadding(8);
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cell);
                
                // Prix unitaire
                cell = new PdfPCell(new Phrase(DECIMAL_FORMAT.format(ligne.getPrixUnitaire()) + " €", dataFont));
                cell.setBackgroundColor(bgColor);
                cell.setPadding(8);
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cell);
                
                // Total ligne
                cell = new PdfPCell(new Phrase(DECIMAL_FORMAT.format(ligne.getTotal()) + " €", dataFont));
                cell.setBackgroundColor(bgColor);
                cell.setPadding(8);
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cell);
                
                // Colonne vide
                cell = new PdfPCell(new Phrase("", dataFont));
                cell.setBackgroundColor(bgColor);
                cell.setPadding(8);
                table.addCell(cell);
                
                alternate = !alternate;
            }
        }
        
        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void ajouterTotaux(Document document, double totalHT, double tauxTVA, 
                               double montantTVA, double totalTTC) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(40);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.setWidths(new float[]{1, 1});
        
        Font labelFont = new Font(Font.HELVETICA, 11, Font.NORMAL, DARK_COLOR);
        Font valueFont = new Font(Font.HELVETICA, 11, Font.BOLD, DARK_COLOR);
        Font totalFont = new Font(Font.HELVETICA, 14, Font.BOLD, PRIMARY_COLOR);
        
        // Total HT
        ajouterLigneTotaux(table, "Total HT", DECIMAL_FORMAT.format(totalHT) + " €", labelFont, valueFont);
        
        // TVA
        ajouterLigneTotaux(table, "TVA (" + DECIMAL_FORMAT.format(tauxTVA) + "%)", 
            DECIMAL_FORMAT.format(montantTVA) + " €", labelFont, valueFont);
        
        // Total TTC
        PdfPCell labelCell = new PdfPCell(new Phrase("TOTAL TTC", totalFont));
        labelCell.setBackgroundColor(LIGHT_GRAY);
        labelCell.setPadding(10);
        labelCell.setBorderColor(PRIMARY_COLOR);
        labelCell.setBorderWidth(2);
        table.addCell(labelCell);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(DECIMAL_FORMAT.format(totalTTC) + " €", totalFont));
        valueCell.setBackgroundColor(LIGHT_GRAY);
        valueCell.setPadding(10);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setBorderColor(PRIMARY_COLOR);
        valueCell.setBorderWidth(2);
        table.addCell(valueCell);
        
        document.add(table);
        document.add(new Paragraph("\n\n"));
    }

    private void ajouterLigneTotaux(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setPadding(8);
        labelCell.setBorderColor(new Color(226, 232, 240));
        table.addCell(labelCell);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setPadding(8);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setBorderColor(new Color(226, 232, 240));
        table.addCell(valueCell);
    }

    private void ajouterConditionsDevis(Document document, Devis devis) throws DocumentException {
        Font titleFont = new Font(Font.HELVETICA, 11, Font.BOLD, DARK_COLOR);
        Font textFont = new Font(Font.HELVETICA, 9, Font.NORMAL, GRAY_COLOR);
        
        Paragraph titre = new Paragraph("CONDITIONS", titleFont);
        document.add(titre);
        
        StringBuilder conditions = new StringBuilder();
        conditions.append("• Ce devis est valable jusqu'au ");
        if (devis.getDateValidite() != null) {
            conditions.append(devis.getDateValidite().format(DATE_FORMATTER));
        } else {
            conditions.append("30 jours après émission");
        }
        conditions.append("\n• Devis gratuit et sans engagement");
        conditions.append("\n• Bon pour accord - Date et signature :");
        
        Paragraph conditionsPara = new Paragraph(conditions.toString(), textFont);
        conditionsPara.setSpacingBefore(5);
        document.add(conditionsPara);
        
        // Zone signature
        document.add(new Paragraph("\n"));
        PdfPTable sigTable = new PdfPTable(1);
        sigTable.setWidthPercentage(40);
        sigTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        
        PdfPCell sigCell = new PdfPCell();
        sigCell.setMinimumHeight(60);
        sigCell.setBorderColor(new Color(226, 232, 240));
        sigTable.addCell(sigCell);
        
        document.add(sigTable);
        document.add(new Paragraph("\n"));
    }

    private void ajouterMentionsLegales(Document document, Entreprise entreprise) throws DocumentException {
        Font footerFont = new Font(Font.HELVETICA, 8, Font.NORMAL, GRAY_COLOR);
        
        Paragraph footer = new Paragraph();
        footer.setAlignment(Element.ALIGN_CENTER);
        
        if (entreprise != null) {
            String mentions = entreprise.getNom();
            if (entreprise.getMatriculeFiscal() != null) {
                mentions += " - MF: " + entreprise.getMatriculeFiscal();
            }
            if (entreprise.getAdresseComplete() != null) {
                mentions += "\n" + entreprise.getAdresseComplete();
            }
            if (entreprise.getTelephone() != null) {
                mentions += " - Tél: " + entreprise.getTelephone();
            }
            if (entreprise.getEmail() != null) {
                mentions += " - " + entreprise.getEmail();
            }
            footer.add(new Chunk(mentions, footerFont));
        }
        
        footer.add(new Chunk("\nDocument généré par DeviSmart", footerFont));
        
        document.add(footer);
    }
}
