package com.example.DeviSmart.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
public class Devis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numero; // ex: DEV-2026-001
    private LocalDate dateCreation;
    private LocalDate dateValidite;
    private String statut; // BROUILLON, VALIDÉ, REFUSÉ
    
    // Taux de TVA applicable (en pourcentage, ex: 19.0)
    private double tauxTVA = 19.0;

    @ManyToOne
    @JoinColumn(name = "client_id")
    @JsonIgnoreProperties({"devis", "factures"})
    private Client client;

    @OneToMany(mappedBy = "devis", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"devis", "facture"})
    private List<Ligne> lignes;

    // Constructors
    public Devis() {}

    public Devis(String numero, LocalDate dateCreation, LocalDate dateValidite, String statut, Client client) {
        this.numero = numero;
        this.dateCreation = dateCreation;
        this.dateValidite = dateValidite;
        this.statut = statut;
        this.client = client;
    }

    // Calcul du total HT
    public double getTotalHT() {
        if (lignes == null) return 0;
        return lignes.stream().mapToDouble(Ligne::getTotal).sum();
    }

    // Calcul du montant TVA
    public double getMontantTVA() {
        return getTotalHT() * (tauxTVA / 100);
    }

    // Calcul du total TTC
    public double getTotalTTC() {
        return getTotalHT() + getMontantTVA();
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDate getDateValidite() {
        return dateValidite;
    }

    public void setDateValidite(LocalDate dateValidite) {
        this.dateValidite = dateValidite;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public List<Ligne> getLignes() {
        return lignes;
    }

    public void setLignes(List<Ligne> lignes) {
        this.lignes = lignes;
    }

    public double getTauxTVA() {
        return tauxTVA;
    }

    public void setTauxTVA(double tauxTVA) {
        this.tauxTVA = tauxTVA;
    }
}
