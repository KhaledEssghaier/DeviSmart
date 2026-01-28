package com.example.DeviSmart.controllers;

import com.example.DeviSmart.entities.Ligne;
import com.example.DeviSmart.services.LigneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lignes")
@CrossOrigin(origins = "*")
public class LigneController {

    @Autowired
    private LigneService ligneService;

    @GetMapping
    public List<Ligne> getAllLignes() {
        return ligneService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ligne> getLigneById(@PathVariable Long id) {
        return ligneService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/devis/{devisId}")
    public List<Ligne> getLignesByDevis(@PathVariable Long devisId) {
        return ligneService.findByDevisId(devisId);
    }

    @GetMapping("/facture/{factureId}")
    public List<Ligne> getLignesByFacture(@PathVariable Long factureId) {
        return ligneService.findByFactureId(factureId);
    }

    @PostMapping
    public Ligne createLigne(@RequestBody Ligne ligne) {
        return ligneService.save(ligne);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ligne> updateLigne(@PathVariable Long id, @RequestBody Ligne ligneDetails) {
        try {
            Ligne updatedLigne = ligneService.update(id, ligneDetails);
            return ResponseEntity.ok(updatedLigne);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLigne(@PathVariable Long id) {
        ligneService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
