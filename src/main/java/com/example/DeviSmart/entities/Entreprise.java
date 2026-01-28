package com.example.DeviSmart.entities;

import jakarta.persistence.*;

@Entity
public class Entreprise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String adresse;
    private String codePostal;
    private String ville;
    private String telephone;
    private String email;
    private String matriculeFiscal;
    private String registreCommerce;
    private String siteWeb;
    
    // Taux de TVA par défaut (en pourcentage)
    private double tauxTVA = 19.0;

    // Compteurs pour la numérotation automatique
    private int compteurDevis = 0;
    private int compteurFactures = 0;

    // Constructors
    public Entreprise() {}

    public Entreprise(String nom, String adresse, String codePostal, String ville, 
                      String telephone, String email, String matriculeFiscal) {
        this.nom = nom;
        this.adresse = adresse;
        this.codePostal = codePostal;
        this.ville = ville;
        this.telephone = telephone;
        this.email = email;
        this.matriculeFiscal = matriculeFiscal;
    }

    // Méthode pour générer le prochain numéro de devis
    public String genererNumeroDevis() {
        compteurDevis++;
        int annee = java.time.Year.now().getValue();
        return String.format("DEV-%d-%04d", annee, compteurDevis);
    }

    // Méthode pour générer le prochain numéro de facture
    public String genererNumeroFacture() {
        compteurFactures++;
        int annee = java.time.Year.now().getValue();
        return String.format("FAC-%d-%04d", annee, compteurFactures);
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getCodePostal() {
        return codePostal;
    }

    public void setCodePostal(String codePostal) {
        this.codePostal = codePostal;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getSiteWeb() {
        return siteWeb;
    }

    public void setSiteWeb(String siteWeb) {
        this.siteWeb = siteWeb;
    }

    public double getTauxTVA() {
        return tauxTVA;
    }

    public void setTauxTVA(double tauxTVA) {
        this.tauxTVA = tauxTVA;
    }

    public int getCompteurDevis() {
        return compteurDevis;
    }

    public void setCompteurDevis(int compteurDevis) {
        this.compteurDevis = compteurDevis;
    }

    public int getCompteurFactures() {
        return compteurFactures;
    }

    public void setCompteurFactures(int compteurFactures) {
        this.compteurFactures = compteurFactures;
    }

    public String getAdresseComplete() {
        return adresse + ", " + codePostal + " " + ville;
    }
}
