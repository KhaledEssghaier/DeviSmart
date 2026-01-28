package com.example.DeviSmart.services;

import com.example.DeviSmart.entities.*;
import com.example.DeviSmart.repositories.DevisRepository;
import com.example.DeviSmart.repositories.FactureRepository;
import com.example.DeviSmart.repositories.LigneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DevisService {

    @Autowired
    private DevisRepository devisRepository;

    @Autowired
    private FactureRepository factureRepository;

    @Autowired
    private LigneRepository ligneRepository;

    @Autowired
    private EntrepriseService entrepriseService;

    public List<Devis> findAll() {
        return devisRepository.findAll();
    }

    public Optional<Devis> findById(Long id) {
        return devisRepository.findById(id);
    }

    public List<Devis> findByClientId(Long clientId) {
        return devisRepository.findByClientId(clientId);
    }

    public List<Devis> findByStatut(String statut) {
        return devisRepository.findByStatut(statut);
    }

    public Devis save(Devis devis) {
        if (devis.getNumero() == null || devis.getNumero().isEmpty()) {
            devis.setNumero(entrepriseService.genererNumeroDevis());
        }
        if (devis.getDateCreation() == null) {
            devis.setDateCreation(LocalDate.now());
        }
        if (devis.getStatut() == null) {
            devis.setStatut("BROUILLON");
        }
        // Appliquer le taux de TVA de l'entreprise
        devis.setTauxTVA(entrepriseService.getTauxTVA());
        return devisRepository.save(devis);
    }

    public Devis update(Long id, Devis devisDetails) {
        Devis devis = devisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Devis non trouvé avec l'id: " + id));
        
        devis.setDateValidite(devisDetails.getDateValidite());
        devis.setStatut(devisDetails.getStatut());
        devis.setClient(devisDetails.getClient());
        
        return devisRepository.save(devis);
    }

    public void delete(Long id) {
        devisRepository.deleteById(id);
    }

    @Transactional
    public Facture validerDevis(Long devisId) {
        Devis devis = devisRepository.findById(devisId)
                .orElseThrow(() -> new RuntimeException("Devis non trouvé avec l'id: " + devisId));
        
        // Mettre à jour le statut du devis
        devis.setStatut("VALIDÉ");
        devisRepository.save(devis);

        // Récupérer les infos entreprise
        Entreprise entreprise = entrepriseService.getEntreprise();

        // Créer la facture correspondante avec la nouvelle structure
        Facture facture = new Facture();
        facture.setNumeroFacture(entrepriseService.genererNumeroFacture());
        facture.setDateEmission(LocalDate.now());
        facture.setDateEcheance(LocalDate.now().plusDays(30));
        facture.setStatut("NON_PAYEE");
        facture.setReferenceDevis(devis.getNumero());
        
        // Copier les données entreprise (immutables)
        facture.copierDonneesEntreprise(entreprise);
        
        // Copier les données client (immutables)
        if (devis.getClient() != null) {
            facture.copierDonneesClient(devis.getClient());
        }

        // Convertir les lignes du devis en LigneFacture
        if (devis.getLignes() != null) {
            for (Ligne ligneDevis : devis.getLignes()) {
                LigneFacture ligneFacture = new LigneFacture();
                ligneFacture.setDesignation(ligneDevis.getDesignation());
                ligneFacture.setQuantite(BigDecimal.valueOf(ligneDevis.getQuantite()));
                ligneFacture.setPrixUnitaireHT(BigDecimal.valueOf(ligneDevis.getPrixUnitaire()));
                ligneFacture.calculerTotal();
                facture.ajouterLigne(ligneFacture);
            }
        }

        // Calculer les totaux
        facture.recalculerTotaux();
        
        return factureRepository.save(facture);
    }

    public Devis refuserDevis(Long devisId) {
        Devis devis = devisRepository.findById(devisId)
                .orElseThrow(() -> new RuntimeException("Devis non trouvé avec l'id: " + devisId));
        
        devis.setStatut("REFUSÉ");
        return devisRepository.save(devis);
    }

    // Calcul du total HT d'un devis
    public double calculerTotalHT(Long devisId) {
        Devis devis = devisRepository.findById(devisId)
                .orElseThrow(() -> new RuntimeException("Devis non trouvé"));
        return devis.getTotalHT();
    }

    // Calcul du total TTC d'un devis avec TVA dynamique
    public double calculerTotalTTC(Long devisId) {
        Devis devis = devisRepository.findById(devisId)
                .orElseThrow(() -> new RuntimeException("Devis non trouvé"));
        return devis.getTotalTTC();
    }

    private String generateNumero() {
        long count = devisRepository.count() + 1;
        return "DEV-" + Year.now().getValue() + "-" + String.format("%03d", count);
    }
}
