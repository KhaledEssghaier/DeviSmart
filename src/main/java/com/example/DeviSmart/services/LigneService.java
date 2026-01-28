package com.example.DeviSmart.services;

import com.example.DeviSmart.entities.Ligne;
import com.example.DeviSmart.repositories.LigneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LigneService {

    @Autowired
    private LigneRepository ligneRepository;

    public List<Ligne> findAll() {
        return ligneRepository.findAll();
    }

    public Optional<Ligne> findById(Long id) {
        return ligneRepository.findById(id);
    }

    public List<Ligne> findByDevisId(Long devisId) {
        return ligneRepository.findByDevisId(devisId);
    }

    public List<Ligne> findByFactureId(Long factureId) {
        return ligneRepository.findByFactureId(factureId);
    }

    public Ligne save(Ligne ligne) {
        return ligneRepository.save(ligne);
    }

    public Ligne update(Long id, Ligne ligneDetails) {
        Ligne ligne = ligneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ligne non trouvée avec l'id: " + id));
        
        ligne.setDesignation(ligneDetails.getDesignation());
        ligne.setQuantite(ligneDetails.getQuantite());
        ligne.setPrixUnitaire(ligneDetails.getPrixUnitaire());
        
        return ligneRepository.save(ligne);
    }

    public void delete(Long id) {
        ligneRepository.deleteById(id);
    }
}
