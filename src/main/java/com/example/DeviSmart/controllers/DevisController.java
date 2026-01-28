package com.example.DeviSmart.controllers;

import com.example.DeviSmart.entities.Devis;
import com.example.DeviSmart.entities.Facture;
import com.example.DeviSmart.services.DevisService;
import com.example.DeviSmart.services.PdfGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/devis")
@CrossOrigin(origins = "*")
public class DevisController {

    @Autowired
    private DevisService devisService;

    @Autowired
    private PdfGenerationService pdfGenerationService;

    @GetMapping
    public List<Devis> getAllDevis() {
        return devisService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Devis> getDevisById(@PathVariable Long id) {
        return devisService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/client/{clientId}")
    public List<Devis> getDevisByClient(@PathVariable Long clientId) {
        return devisService.findByClientId(clientId);
    }

    @GetMapping("/statut/{statut}")
    public List<Devis> getDevisByStatut(@PathVariable String statut) {
        return devisService.findByStatut(statut);
    }

    @PostMapping
    public Devis createDevis(@RequestBody Devis devis) {
        return devisService.save(devis);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Devis> updateDevis(@PathVariable Long id, @RequestBody Devis devisDetails) {
        try {
            Devis updatedDevis = devisService.update(id, devisDetails);
            return ResponseEntity.ok(updatedDevis);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevis(@PathVariable Long id) {
        devisService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/valider")
    public ResponseEntity<Facture> validerDevis(@PathVariable Long id) {
        try {
            Facture facture = devisService.validerDevis(id);
            return ResponseEntity.ok(facture);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/refuser")
    public ResponseEntity<Devis> refuserDevis(@PathVariable Long id) {
        try {
            Devis devis = devisService.refuserDevis(id);
            return ResponseEntity.ok(devis);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/totaux")
    public ResponseEntity<Map<String, Double>> getTotaux(@PathVariable Long id) {
        try {
            double totalHT = devisService.calculerTotalHT(id);
            double totalTTC = devisService.calculerTotalTTC(id);
            return ResponseEntity.ok(Map.of(
                    "totalHT", totalHT,
                    "totalTTC", totalTTC,
                    "tva", totalTTC - totalHT
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Génère et télécharge le PDF d'un devis
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> genererPdf(@PathVariable Long id) {
        try {
            Devis devis = devisService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Devis non trouvé"));
            
            byte[] pdfContent = pdfGenerationService.genererPdfDevis(devis);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                "devis_" + devis.getNumero().replace("/", "-") + ".pdf");
            headers.setContentLength(pdfContent.length);
            
            return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
