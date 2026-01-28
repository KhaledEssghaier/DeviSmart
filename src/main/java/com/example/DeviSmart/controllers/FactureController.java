package com.example.DeviSmart.controllers;

import com.example.DeviSmart.entities.Facture;
import com.example.DeviSmart.entities.LigneFacture;
import com.example.DeviSmart.services.FactureService;
import com.example.DeviSmart.services.FacturePdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST pour la gestion des factures
 * 
 * Endpoints pour CRUD, statuts, calculs et génération PDF
 */
@RestController
@RequestMapping("/api/factures")
@CrossOrigin(origins = "*")
public class FactureController {

    @Autowired
    private FactureService factureService;

    @Autowired
    private FacturePdfService facturePdfService;

    // ==================== LECTURE ====================

    @GetMapping
    public List<Facture> getAllFactures() {
        return factureService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Facture> getFactureById(@PathVariable Long id) {
        return factureService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/client/{clientId}")
    public List<Facture> getFacturesByClient(@PathVariable Long clientId) {
        return factureService.findByClientId(clientId);
    }

    @GetMapping("/statut/{statut}")
    public List<Facture> getFacturesByStatut(@PathVariable String statut) {
        return factureService.findByStatut(statut);
    }

    @GetMapping("/numero/{numero}")
    public ResponseEntity<Facture> getFactureByNumero(@PathVariable String numero) {
        return factureService.findByNumero(numero)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ==================== CRÉATION ====================

    /**
     * Crée une nouvelle facture avec calculs automatiques
     * Body: { "clientId": 1, "lignes": [...] }
     */
    @PostMapping("/creer")
    public ResponseEntity<Facture> creerFacture(@RequestBody CreerFactureRequest request) {
        try {
            Facture facture = factureService.creerFacture(request.clientId, request.lignes);
            return ResponseEntity.status(HttpStatus.CREATED).body(facture);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Ancienne méthode de création (deprecated, pour compatibilité)
     */
    @PostMapping
    public Facture createFacture(@RequestBody Facture facture) {
        return factureService.save(facture);
    }

    /**
     * Crée une facture manuelle sans client existant
     * Body: { "nomClient": "...", "adresseClient": "...", "lignes": [...] }
     */
    @PostMapping("/creer-manuelle")
    public ResponseEntity<Facture> creerFactureManuelle(@RequestBody CreerFactureManuelleRequest request) {
        try {
            Facture facture = factureService.creerFactureManuelle(
                request.nomClient,
                request.adresseClient,
                request.telephoneClient,
                request.emailClient,
                request.matriculeFiscalClient,
                request.tauxTVA,
                request.conditionsPaiement,
                request.notes,
                request.lignes
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(facture);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ==================== MODIFICATION ====================

    @PutMapping("/{id}")
    public ResponseEntity<Facture> updateFacture(@PathVariable Long id, @RequestBody Facture factureDetails) {
        try {
            Facture updatedFacture = factureService.update(id, factureDetails);
            return ResponseEntity.ok(updatedFacture);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Ajoute une ligne à une facture existante
     */
    @PostMapping("/{id}/lignes")
    public ResponseEntity<Facture> ajouterLigne(@PathVariable Long id, @RequestBody LigneFacture ligne) {
        try {
            Facture facture = factureService.ajouterLigne(id, ligne);
            return ResponseEntity.ok(facture);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Met à jour une ligne existante
     */
    @PutMapping("/{id}/lignes/{ligneId}")
    public ResponseEntity<Facture> modifierLigne(
            @PathVariable Long id,
            @PathVariable Long ligneId,
            @RequestBody LigneFacture ligne) {
        try {
            Facture facture = factureService.mettreAJourLigne(id, ligneId, ligne);
            return ResponseEntity.ok(facture);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Supprime une ligne
     */
    @DeleteMapping("/{id}/lignes/{ligneId}")
    public ResponseEntity<Facture> supprimerLigne(@PathVariable Long id, @PathVariable Long ligneId) {
        try {
            Facture facture = factureService.supprimerLigne(id, ligneId);
            return ResponseEntity.ok(facture);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ==================== SUPPRESSION ====================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFacture(@PathVariable Long id) {
        factureService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== STATUTS ====================

    @PostMapping("/{id}/payer")
    public ResponseEntity<Facture> marquerPayee(@PathVariable Long id) {
        try {
            Facture facture = factureService.marquerPayee(id);
            return ResponseEntity.ok(facture);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/impayer")
    public ResponseEntity<Facture> marquerNonPayee(@PathVariable Long id) {
        try {
            Facture facture = factureService.marquerNonPayee(id);
            return ResponseEntity.ok(facture);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/retard")
    public ResponseEntity<Facture> marquerEnRetard(@PathVariable Long id) {
        try {
            Facture facture = factureService.marquerEnRetard(id);
            return ResponseEntity.ok(facture);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/annuler")
    public ResponseEntity<Facture> annulerFacture(@PathVariable Long id) {
        try {
            Facture facture = factureService.annuler(id);
            return ResponseEntity.ok(facture);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ==================== CALCULS ====================

    @GetMapping("/{id}/totaux")
    public ResponseEntity<Map<String, Object>> getTotaux(@PathVariable Long id) {
        try {
            BigDecimal totalHT = factureService.getTotalHT(id);
            BigDecimal totalTTC = factureService.getTotalTTC(id);
            BigDecimal tva = totalTTC.subtract(totalHT);
            
            return ResponseEntity.ok(Map.of(
                    "totalHT", totalHT,
                    "totalTTC", totalTTC,
                    "tva", tva
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/recalculer")
    public ResponseEntity<Facture> recalculerTotaux(@PathVariable Long id) {
        try {
            Facture facture = factureService.recalculerTotaux(id);
            return ResponseEntity.ok(facture);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ==================== STATISTIQUES ====================

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistiques() {
        return ResponseEntity.ok(Map.of(
                "chiffreAffaires", factureService.getChiffreAffaires(),
                "montantEnAttente", factureService.getMontantEnAttente(),
                "montantEnRetard", factureService.getMontantEnRetard()
        ));
    }

    // ==================== PDF ====================

    /**
     * Génère et télécharge le PDF d'une facture
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> genererPdf(@PathVariable Long id) {
        try {
            Facture facture = factureService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Facture non trouvée"));
            
            byte[] pdfContent = facturePdfService.generer(facture);
            
            String filename = "facture_" + facture.getNumeroFacture().replace("/", "-") + ".pdf";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfContent.length);
            
            return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ==================== DTOs INTERNES ====================

    /**
     * DTO pour la création de facture
     */
    public static class CreerFactureRequest {
        public Long clientId;
        public List<LigneFacture> lignes;
    }

    /**
     * DTO pour la création de facture manuelle (sans client)
     */
    public static class CreerFactureManuelleRequest {
        public String nomClient;
        public String adresseClient;
        public String telephoneClient;
        public String emailClient;
        public String matriculeFiscalClient;
        public BigDecimal tauxTVA;
        public String conditionsPaiement;
        public String notes;
        public List<LigneFacture> lignes;
    }
}
