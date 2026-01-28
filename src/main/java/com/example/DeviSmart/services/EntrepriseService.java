package com.example.DeviSmart.services;

import com.example.DeviSmart.entities.Entreprise;
import com.example.DeviSmart.repositories.EntrepriseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EntrepriseService {

    @Autowired
    private EntrepriseRepository entrepriseRepository;

    /**
     * Récupère l'entreprise (configuration unique)
     */
    public Entreprise getEntreprise() {
        return entrepriseRepository.getEntreprise();
    }

    /**
     * Crée ou met à jour l'entreprise
     */
    public Entreprise saveEntreprise(Entreprise entreprise) {
        Entreprise existante = entrepriseRepository.getEntreprise();
        if (existante != null) {
            entreprise.setId(existante.getId());
            // Préserver les compteurs
            entreprise.setCompteurDevis(existante.getCompteurDevis());
            entreprise.setCompteurFactures(existante.getCompteurFactures());
        }
        return entrepriseRepository.save(entreprise);
    }

    /**
     * Génère le prochain numéro de devis
     */
    public String genererNumeroDevis() {
        Entreprise entreprise = getOrCreateEntreprise();
        String numero = entreprise.genererNumeroDevis();
        entrepriseRepository.save(entreprise);
        return numero;
    }

    /**
     * Génère le prochain numéro de facture
     */
    public String genererNumeroFacture() {
        Entreprise entreprise = getOrCreateEntreprise();
        String numero = entreprise.genererNumeroFacture();
        entrepriseRepository.save(entreprise);
        return numero;
    }

    /**
     * Récupère le taux de TVA configuré
     */
    public double getTauxTVA() {
        Entreprise entreprise = entrepriseRepository.getEntreprise();
        return entreprise != null ? entreprise.getTauxTVA() : 19.0;
    }

    /**
     * Récupère ou crée l'entreprise par défaut
     */
    private Entreprise getOrCreateEntreprise() {
        Entreprise entreprise = entrepriseRepository.getEntreprise();
        if (entreprise == null) {
            entreprise = new Entreprise();
            entreprise.setNom("DeviSmart");
            entreprise.setAdresse("123 Rue de l'Innovation");
            entreprise.setCodePostal("75001");
            entreprise.setVille("Paris");
            entreprise.setTelephone("+33 1 23 45 67 89");
            entreprise.setEmail("contact@devismart.com");
            entreprise.setMatriculeFiscal("FR12345678901");
            entreprise.setTauxTVA(19.0);
            entreprise = entrepriseRepository.save(entreprise);
        }
        return entreprise;
    }

    /**
     * Initialise les données par défaut au démarrage
     */
    public void initialiserDonneesParDefaut() {
        getOrCreateEntreprise();
    }
}
