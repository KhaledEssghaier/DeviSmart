package com.example.DeviSmart.repositories;

import com.example.DeviSmart.entities.Entreprise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EntrepriseRepository extends JpaRepository<Entreprise, Long> {
    // Récupérer la première entreprise (configuration unique)
    default Entreprise getEntreprise() {
        return findAll().stream().findFirst().orElse(null);
    }
}
