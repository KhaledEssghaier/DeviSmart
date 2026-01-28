package com.example.DeviSmart.controllers;

import com.example.DeviSmart.entities.Entreprise;
import com.example.DeviSmart.services.EntrepriseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/entreprise")
@CrossOrigin(origins = "http://localhost:4200")
public class EntrepriseController {

    @Autowired
    private EntrepriseService entrepriseService;

    /**
     * Récupère les informations de l'entreprise
     */
    @GetMapping
    public ResponseEntity<Entreprise> getEntreprise() {
        Entreprise entreprise = entrepriseService.getEntreprise();
        if (entreprise == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(entreprise);
    }

    /**
     * Crée ou met à jour les informations de l'entreprise
     */
    @PostMapping
    public ResponseEntity<Entreprise> saveEntreprise(@RequestBody Entreprise entreprise) {
        return ResponseEntity.ok(entrepriseService.saveEntreprise(entreprise));
    }

    /**
     * Met à jour les informations de l'entreprise
     */
    @PutMapping
    public ResponseEntity<Entreprise> updateEntreprise(@RequestBody Entreprise entreprise) {
        return ResponseEntity.ok(entrepriseService.saveEntreprise(entreprise));
    }

    /**
     * Récupère le taux de TVA configuré
     */
    @GetMapping("/tva")
    public ResponseEntity<Double> getTauxTVA() {
        return ResponseEntity.ok(entrepriseService.getTauxTVA());
    }
}
