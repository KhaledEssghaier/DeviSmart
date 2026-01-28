package com.example.DeviSmart.config;

import com.example.DeviSmart.services.EntrepriseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Initialise les données par défaut au démarrage de l'application
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private EntrepriseService entrepriseService;

    @Override
    public void run(String... args) {
        // Initialiser l'entreprise par défaut si elle n'existe pas
        entrepriseService.initialiserDonneesParDefaut();
        System.out.println("✅ DeviSmart initialisé avec succès !");
    }
}
