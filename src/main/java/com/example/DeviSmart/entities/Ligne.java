package com.example.DeviSmart.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
public class Ligne {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String designation;
    private int quantite;
    private double prixUnitaire;

    @ManyToOne
    @JoinColumn(name = "devis_id")
    @JsonIgnoreProperties({"lignes", "client"})
    private Devis devis;

    @ManyToOne
    @JoinColumn(name = "facture_id")
    @JsonIgnoreProperties({"lignes", "client"})
    private Facture facture;

    // Constructors
    public Ligne() {}

    public Ligne(String designation, int quantite, double prixUnitaire) {
        this.designation = designation;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
    }

    // Calcul du total de la ligne
    public double getTotal() {
        return quantite * prixUnitaire;
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

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public double getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(double prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public Devis getDevis() {
        return devis;
    }

    public void setDevis(Devis devis) {
        this.devis = devis;
    }

    public Facture getFacture() {
        return facture;
    }

    public void setFacture(Facture facture) {
        this.facture = facture;
    }
}
