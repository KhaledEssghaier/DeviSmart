package com.example.DeviSmart.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité Facture - Conforme aux exigences légales
 * 
 * Les données entreprise et client sont intégrées (dénormalisées) pour garantir
 * l'immutabilité des documents légaux et faciliter la génération PDF.
 */
@Entity
@Table(name = "factures")
public class Facture {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==================== NUMÉRO UNIQUE ====================
    @Column(unique = true, nullable = false)
    private String numeroFacture; // ex: FAC-2025-0001

    // ==================== DATES ====================
    @Column(nullable = false)
    private LocalDate dateEmission;
    
    private LocalDate dateEcheance;

    // ==================== STATUT ====================
    @Column(nullable = false)
    private String statut = "NON_PAYEE"; // NON_PAYEE, PAYEE, EN_RETARD, ANNULEE

    // ==================== RÉFÉRENCE DEVIS ====================
    private String referenceDevis;

    // ==================== DONNÉES ENTREPRISE (intégrées) ====================
    @Column(nullable = false)
    private String nomEntreprise;
    
    @Column(length = 500)
    private String adresseEntreprise;
    
    private String telephoneEntreprise;
    private String emailEntreprise;
    private String matriculeFiscal;
    private String registreCommerce;

    // ==================== DONNÉES CLIENT (intégrées) ====================
    @Column(nullable = false)
    private String nomClient;
    
    @Column(length = 500)
    private String adresseClient;
    
    private String telephoneClient;
    private String emailClient;
    private String matriculeFiscalClient;

    // ==================== MONTANTS CALCULÉS (stockés) ====================
    @Column(precision = 15, scale = 3)
    private BigDecimal totalHT = BigDecimal.ZERO;
    
    @Column(precision = 5, scale = 4)
    private BigDecimal tauxTVA = new BigDecimal("0.19"); // Format décimal: 0.19 = 19%
    
    @Column(precision = 15, scale = 3)
    private BigDecimal montantTVA = BigDecimal.ZERO;
    
    @Column(precision = 15, scale = 3)
    private BigDecimal totalTTC = BigDecimal.ZERO;

    // ==================== CONDITIONS ====================
    @Column(length = 1000)
    private String conditionsPaiement;
    
    @Column(length = 2000)
    private String notes;

    // ==================== RÉFÉRENCE CLIENT (pour historique) ====================
    @ManyToOne
    @JoinColumn(name = "client_id")
    @JsonIgnoreProperties({"devis", "factures"})
    private Client client;

    // ==================== LIGNES DE FACTURE ====================
    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("facture")
    private List<LigneFacture> lignes = new ArrayList<>();

    // ==================== CONSTRUCTEURS ====================
    public Facture() {}

    public Facture(String numeroFacture, LocalDate dateEmission) {
        this.numeroFacture = numeroFacture;
        this.dateEmission = dateEmission;
    }

    // ==================== MÉTHODES MÉTIER ====================
    
    /**
     * Recalcule tous les totaux de la facture
     * Cette méthode doit être appelée par le service après modification des lignes
     */
    public void recalculerTotaux() {
        // Calculer total HT
        this.totalHT = lignes.stream()
                .map(LigneFacture::getTotalLigneHT)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculer montant TVA
        this.montantTVA = this.totalHT.multiply(this.tauxTVA)
                .setScale(3, RoundingMode.HALF_UP);
        
        // Calculer total TTC
        this.totalTTC = this.totalHT.add(this.montantTVA)
                .setScale(3, RoundingMode.HALF_UP);
    }

    /**
     * Ajoute une ligne à la facture et met à jour la relation bidirectionnelle
     */
    public void ajouterLigne(LigneFacture ligne) {
        lignes.add(ligne);
        ligne.setFacture(this);
    }

    /**
     * Retire une ligne de la facture
     */
    public void retirerLigne(LigneFacture ligne) {
        lignes.remove(ligne);
        ligne.setFacture(null);
    }

    /**
     * Copie les données depuis une entité Entreprise
     */
    public void copierDonneesEntreprise(Entreprise entreprise) {
        if (entreprise != null) {
            this.nomEntreprise = entreprise.getNom();
            this.adresseEntreprise = entreprise.getAdresse();
            this.telephoneEntreprise = entreprise.getTelephone();
            this.emailEntreprise = entreprise.getEmail();
            this.matriculeFiscal = entreprise.getMatriculeFiscal();
            this.registreCommerce = entreprise.getRegistreCommerce();
            // Récupérer le taux TVA de l'entreprise
            this.tauxTVA = BigDecimal.valueOf(entreprise.getTauxTVA() / 100);
        }
    }

    /**
     * Copie les données depuis une entité Client
     */
    public void copierDonneesClient(Client client) {
        if (client != null) {
            this.client = client;
            this.nomClient = client.getNom();
            this.adresseClient = client.getAdresse();
            this.telephoneClient = client.getTelephone();
            this.emailClient = client.getEmail();
            this.matriculeFiscalClient = client.getMatriculeFiscal();
        }
    }

    /**
     * Retourne le taux TVA en pourcentage (ex: 19.0 pour 19%)
     */
    public double getTauxTVAPourcentage() {
        return tauxTVA.multiply(new BigDecimal("100")).doubleValue();
    }

    // ==================== GETTERS & SETTERS ====================
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroFacture() {
        return numeroFacture;
    }

    public void setNumeroFacture(String numeroFacture) {
        this.numeroFacture = numeroFacture;
    }

    public LocalDate getDateEmission() {
        return dateEmission;
    }

    public void setDateEmission(LocalDate dateEmission) {
        this.dateEmission = dateEmission;
    }

    public LocalDate getDateEcheance() {
        return dateEcheance;
    }

    public void setDateEcheance(LocalDate dateEcheance) {
        this.dateEcheance = dateEcheance;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getReferenceDevis() {
        return referenceDevis;
    }

    public void setReferenceDevis(String referenceDevis) {
        this.referenceDevis = referenceDevis;
    }

    public String getNomEntreprise() {
        return nomEntreprise;
    }

    public void setNomEntreprise(String nomEntreprise) {
        this.nomEntreprise = nomEntreprise;
    }

    public String getAdresseEntreprise() {
        return adresseEntreprise;
    }

    public void setAdresseEntreprise(String adresseEntreprise) {
        this.adresseEntreprise = adresseEntreprise;
    }

    public String getTelephoneEntreprise() {
        return telephoneEntreprise;
    }

    public void setTelephoneEntreprise(String telephoneEntreprise) {
        this.telephoneEntreprise = telephoneEntreprise;
    }

    public String getEmailEntreprise() {
        return emailEntreprise;
    }

    public void setEmailEntreprise(String emailEntreprise) {
        this.emailEntreprise = emailEntreprise;
    }

    public String getMatriculeFiscal() {
        return matriculeFiscal;
    }

    public void setMatriculeFiscal(String matriculeFiscal) {
        this.matriculeFiscal = matriculeFiscal;
    }

    public String getRegistreCommerce() {
        return registreCommerce;
    }

    public void setRegistreCommerce(String registreCommerce) {
        this.registreCommerce = registreCommerce;
    }

    public String getNomClient() {
        return nomClient;
    }

    public void setNomClient(String nomClient) {
        this.nomClient = nomClient;
    }

    public String getAdresseClient() {
        return adresseClient;
    }

    public void setAdresseClient(String adresseClient) {
        this.adresseClient = adresseClient;
    }

    public String getTelephoneClient() {
        return telephoneClient;
    }

    public void setTelephoneClient(String telephoneClient) {
        this.telephoneClient = telephoneClient;
    }

    public String getEmailClient() {
        return emailClient;
    }

    public void setEmailClient(String emailClient) {
        this.emailClient = emailClient;
    }

    public String getMatriculeFiscalClient() {
        return matriculeFiscalClient;
    }

    public void setMatriculeFiscalClient(String matriculeFiscalClient) {
        this.matriculeFiscalClient = matriculeFiscalClient;
    }

    public BigDecimal getTotalHT() {
        return totalHT;
    }

    public void setTotalHT(BigDecimal totalHT) {
        this.totalHT = totalHT;
    }

    public BigDecimal getTauxTVA() {
        return tauxTVA;
    }

    public void setTauxTVA(BigDecimal tauxTVA) {
        this.tauxTVA = tauxTVA;
    }

    public BigDecimal getMontantTVA() {
        return montantTVA;
    }

    public void setMontantTVA(BigDecimal montantTVA) {
        this.montantTVA = montantTVA;
    }

    public BigDecimal getTotalTTC() {
        return totalTTC;
    }

    public void setTotalTTC(BigDecimal totalTTC) {
        this.totalTTC = totalTTC;
    }

    public String getConditionsPaiement() {
        return conditionsPaiement;
    }

    public void setConditionsPaiement(String conditionsPaiement) {
        this.conditionsPaiement = conditionsPaiement;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public List<LigneFacture> getLignes() {
        return lignes;
    }

    public void setLignes(List<LigneFacture> lignes) {
        this.lignes = lignes;
    }

    // Pour compatibilité avec l'ancien code (deprecated)
    @Deprecated
    public String getNumero() {
        return numeroFacture;
    }

    @Deprecated
    public void setNumero(String numero) {
        this.numeroFacture = numero;
    }
}
