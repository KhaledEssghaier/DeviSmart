package com.example.DeviSmart.services;

import com.example.DeviSmart.entities.*;
import com.example.DeviSmart.repositories.FactureRepository;
import com.example.DeviSmart.repositories.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service Facture - Calculs métier centralisés
 * 
 * Toutes les opérations de calcul sont effectuées ici, jamais côté frontend.
 * Garantit la cohérence des données et l'immutabilité des documents légaux.
 */
@Service
@Transactional
public class FactureService {

    @Autowired
    private FactureRepository factureRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private EntrepriseService entrepriseService;

    // ==================== LECTURE ====================

    public List<Facture> findAll() {
        return factureRepository.findAll();
    }

    public Optional<Facture> findById(Long id) {
        return factureRepository.findById(id);
    }

    public List<Facture> findByClientId(Long clientId) {
        return factureRepository.findByClientId(clientId);
    }

    public List<Facture> findByStatut(String statut) {
        return factureRepository.findByStatut(statut);
    }

    public Optional<Facture> findByNumero(String numeroFacture) {
        return factureRepository.findByNumeroFacture(numeroFacture);
    }

    // ==================== CRÉATION ====================

    /**
     * Crée une nouvelle facture avec toutes les données métier
     * 
     * @param clientId ID du client
     * @param lignes Liste des lignes de facture
     * @return Facture créée avec tous les calculs effectués
     */
    public Facture creerFacture(Long clientId, List<LigneFacture> lignes) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client non trouvé avec l'id: " + clientId));
        
        Entreprise entreprise = entrepriseService.getEntreprise();
        
        Facture facture = new Facture();
        
        // Générer le numéro unique
        facture.setNumeroFacture(entrepriseService.genererNumeroFacture());
        facture.setDateEmission(LocalDate.now());
        facture.setDateEcheance(LocalDate.now().plusDays(30)); // Échéance par défaut: 30 jours
        facture.setStatut("NON_PAYEE");
        
        // Copier les données entreprise (immutables)
        facture.copierDonneesEntreprise(entreprise);
        
        // Copier les données client (immutables)
        facture.copierDonneesClient(client);
        
        // Ajouter les lignes
        if (lignes != null) {
            for (LigneFacture ligne : lignes) {
                ligne.calculerTotal(); // S'assurer que chaque ligne est calculée
                facture.ajouterLigne(ligne);
            }
        }
        
        // Calculer les totaux
        facture.recalculerTotaux();
        
        return factureRepository.save(facture);
    }

    /**
     * Crée une facture à partir d'un devis existant
     */
    public Facture creerFactureDepuisDevis(Devis devis) {
        Entreprise entreprise = entrepriseService.getEntreprise();
        
        Facture facture = new Facture();
        
        facture.setNumeroFacture(entrepriseService.genererNumeroFacture());
        facture.setDateEmission(LocalDate.now());
        facture.setDateEcheance(LocalDate.now().plusDays(30));
        facture.setStatut("NON_PAYEE");
        facture.setReferenceDevis(devis.getNumero());
        
        // Copier les données entreprise
        facture.copierDonneesEntreprise(entreprise);
        
        // Copier les données client depuis le devis
        if (devis.getClient() != null) {
            facture.copierDonneesClient(devis.getClient());
        }
        
        // Convertir les lignes du devis en lignes de facture
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

    /**
     * Crée une facture manuelle sans client existant
     * Toutes les données client sont saisies directement
     */
    public Facture creerFactureManuelle(
            String nomClient,
            String adresseClient,
            String telephoneClient,
            String emailClient,
            String matriculeFiscalClient,
            BigDecimal tauxTVA,
            String conditionsPaiement,
            String notes,
            List<LigneFacture> lignes) {
        
        Entreprise entreprise = entrepriseService.getEntreprise();
        
        Facture facture = new Facture();
        
        // Générer le numéro unique
        facture.setNumeroFacture(entrepriseService.genererNumeroFacture());
        facture.setDateEmission(LocalDate.now());
        facture.setDateEcheance(LocalDate.now().plusDays(30));
        facture.setStatut("NON_PAYEE");
        
        // Copier les données entreprise
        facture.copierDonneesEntreprise(entreprise);
        
        // Définir les données client manuellement
        facture.setNomClient(nomClient != null ? nomClient : "Client");
        facture.setAdresseClient(adresseClient);
        facture.setTelephoneClient(telephoneClient);
        facture.setEmailClient(emailClient);
        facture.setMatriculeFiscalClient(matriculeFiscalClient);
        
        // Taux TVA personnalisé
        if (tauxTVA != null) {
            facture.setTauxTVA(tauxTVA);
        }
        
        // Conditions et notes
        facture.setConditionsPaiement(conditionsPaiement);
        facture.setNotes(notes);
        
        // Ajouter les lignes
        if (lignes != null) {
            for (LigneFacture ligne : lignes) {
                ligne.calculerTotal();
                facture.ajouterLigne(ligne);
            }
        }
        
        // Calculer les totaux
        facture.recalculerTotaux();
        
        return factureRepository.save(facture);
    }

    // ==================== MODIFICATION ====================

    /**
     * Met à jour le statut d'une facture
     */
    public Facture mettreAJourStatut(Long factureId, String nouveauStatut) {
        Facture facture = factureRepository.findById(factureId)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée avec l'id: " + factureId));
        
        facture.setStatut(nouveauStatut);
        return factureRepository.save(facture);
    }

    /**
     * Ajoute une ligne à une facture existante et recalcule les totaux
     */
    public Facture ajouterLigne(Long factureId, LigneFacture nouvelleLigne) {
        Facture facture = factureRepository.findById(factureId)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée avec l'id: " + factureId));
        
        nouvelleLigne.calculerTotal();
        facture.ajouterLigne(nouvelleLigne);
        facture.recalculerTotaux();
        
        return factureRepository.save(facture);
    }

    /**
     * Met à jour une ligne existante et recalcule les totaux
     */
    public Facture mettreAJourLigne(Long factureId, Long ligneId, LigneFacture ligneModifiee) {
        Facture facture = factureRepository.findById(factureId)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée avec l'id: " + factureId));
        
        facture.getLignes().stream()
                .filter(l -> l.getId().equals(ligneId))
                .findFirst()
                .ifPresent(ligne -> {
                    ligne.setDesignation(ligneModifiee.getDesignation());
                    ligne.setQuantite(ligneModifiee.getQuantite());
                    ligne.setPrixUnitaireHT(ligneModifiee.getPrixUnitaireHT());
                    ligne.calculerTotal();
                });
        
        facture.recalculerTotaux();
        
        return factureRepository.save(facture);
    }

    /**
     * Supprime une ligne et recalcule les totaux
     */
    public Facture supprimerLigne(Long factureId, Long ligneId) {
        Facture facture = factureRepository.findById(factureId)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée avec l'id: " + factureId));
        
        facture.getLignes().removeIf(l -> l.getId().equals(ligneId));
        facture.recalculerTotaux();
        
        return factureRepository.save(facture);
    }

    // ==================== STATUTS ====================

    public Facture marquerPayee(Long factureId) {
        return mettreAJourStatut(factureId, "PAYEE");
    }

    public Facture marquerNonPayee(Long factureId) {
        return mettreAJourStatut(factureId, "NON_PAYEE");
    }

    public Facture marquerEnRetard(Long factureId) {
        return mettreAJourStatut(factureId, "EN_RETARD");
    }

    public Facture annuler(Long factureId) {
        return mettreAJourStatut(factureId, "ANNULEE");
    }

    // ==================== SUPPRESSION ====================

    public void delete(Long id) {
        factureRepository.deleteById(id);
    }

    // ==================== CALCULS MÉTIER (centralisés) ====================

    /**
     * Recalcule tous les totaux d'une facture
     * Cette méthode est le point central de tous les calculs
     */
    public Facture recalculerTotaux(Long factureId) {
        Facture facture = factureRepository.findById(factureId)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée avec l'id: " + factureId));
        
        // Recalculer chaque ligne
        for (LigneFacture ligne : facture.getLignes()) {
            ligne.calculerTotal();
        }
        
        // Recalculer les totaux de la facture
        facture.recalculerTotaux();
        
        return factureRepository.save(facture);
    }

    /**
     * Retourne le total HT d'une facture
     */
    public BigDecimal getTotalHT(Long factureId) {
        Facture facture = factureRepository.findById(factureId)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée"));
        return facture.getTotalHT();
    }

    /**
     * Retourne le total TTC d'une facture
     */
    public BigDecimal getTotalTTC(Long factureId) {
        Facture facture = factureRepository.findById(factureId)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée"));
        return facture.getTotalTTC();
    }

    // ==================== STATISTIQUES ====================

    /**
     * Calcule le chiffre d'affaires total (factures payées)
     */
    public BigDecimal getChiffreAffaires() {
        return factureRepository.findByStatut("PAYEE").stream()
                .map(Facture::getTotalTTC)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcule le montant total des factures en attente
     */
    public BigDecimal getMontantEnAttente() {
        return factureRepository.findByStatut("NON_PAYEE").stream()
                .map(Facture::getTotalTTC)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcule le montant total des factures en retard
     */
    public BigDecimal getMontantEnRetard() {
        return factureRepository.findByStatut("EN_RETARD").stream()
                .map(Facture::getTotalTTC)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ==================== COMPATIBILITÉ (deprecated) ====================

    @Deprecated
    public Facture save(Facture facture) {
        if (facture.getNumeroFacture() == null || facture.getNumeroFacture().isEmpty()) {
            facture.setNumeroFacture(entrepriseService.genererNumeroFacture());
        }
        if (facture.getDateEmission() == null) {
            facture.setDateEmission(LocalDate.now());
        }
        if (facture.getStatut() == null) {
            facture.setStatut("NON_PAYEE");
        }
        
        // Copier les données entreprise si non présentes
        if (facture.getNomEntreprise() == null) {
            Entreprise entreprise = entrepriseService.getEntreprise();
            facture.copierDonneesEntreprise(entreprise);
        }
        
        // Copier les données client si non présentes
        if (facture.getNomClient() == null && facture.getClient() != null) {
            facture.copierDonneesClient(facture.getClient());
        }
        
        // Recalculer les totaux
        facture.recalculerTotaux();
        
        return factureRepository.save(facture);
    }

    @Deprecated
    public Facture update(Long id, Facture factureDetails) {
        Facture facture = factureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée avec l'id: " + id));
        
        facture.setStatut(factureDetails.getStatut());
        facture.recalculerTotaux();
        
        return factureRepository.save(facture);
    }

    @Deprecated
    public double calculerTotalHT(Long factureId) {
        return getTotalHT(factureId).doubleValue();
    }

    @Deprecated
    public double calculerTotalTTC(Long factureId) {
        return getTotalTTC(factureId).doubleValue();
    }
}
