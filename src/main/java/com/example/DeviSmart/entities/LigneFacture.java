package com.example.DeviSmart.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Entité LigneFacture - Représente une ligne dans une facture
 * Chaque produit/service facturé avec son calcul de total
 */
@Entity
@Table(name = "ligne_facture")
public class LigneFacture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String designation;

    @Column(nullable = false, precision = 15, scale = 3)
    private BigDecimal quantite = BigDecimal.ONE;

    @Column(nullable = false, precision = 15, scale = 3)
    private BigDecimal prixUnitaireHT = BigDecimal.ZERO;

    // Total calculé : quantite × prixUnitaireHT
    @Column(precision = 15, scale = 3)
    private BigDecimal totalLigneHT = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facture_id", nullable = false)
    @JsonIgnoreProperties({"lignes", "hibernateLazyInitializer"})
    private Facture facture;

    // Constructors
    public LigneFacture() {}

    public LigneFacture(String designation, BigDecimal quantite, BigDecimal prixUnitaireHT) {
        this.designation = designation;
        this.quantite = quantite;
        this.prixUnitaireHT = prixUnitaireHT;
        this.calculerTotal();
    }

    // Constructeur pour compatibilité avec int/double
    public LigneFacture(String designation, int quantite, double prixUnitaireHT) {
        this.designation = designation;
        this.quantite = BigDecimal.valueOf(quantite);
        this.prixUnitaireHT = BigDecimal.valueOf(prixUnitaireHT);
        this.calculerTotal();
    }

    /**
     * Calcule le total de la ligne HT
     */
    public void calculerTotal() {
        if (this.quantite != null && this.prixUnitaireHT != null) {
            this.totalLigneHT = this.quantite.multiply(this.prixUnitaireHT)
                    .setScale(3, RoundingMode.HALF_UP);
        }
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public BigDecimal getQuantite() {
        return quantite;
    }

    public void setQuantite(BigDecimal quantite) {
        this.quantite = quantite;
        this.calculerTotal();
    }

    public BigDecimal getPrixUnitaireHT() {
        return prixUnitaireHT;
    }

    public void setPrixUnitaireHT(BigDecimal prixUnitaireHT) {
        this.prixUnitaireHT = prixUnitaireHT;
        this.calculerTotal();
    }

    public BigDecimal getTotalLigneHT() {
        return totalLigneHT;
    }

    public void setTotalLigneHT(BigDecimal totalLigneHT) {
        this.totalLigneHT = totalLigneHT;
    }

    public Facture getFacture() {
        return facture;
    }

    public void setFacture(Facture facture) {
        this.facture = facture;
    }
}
