package com.example.DeviSmart.repositories;

import com.example.DeviSmart.entities.Facture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FactureRepository extends JpaRepository<Facture, Long> {
    List<Facture> findByClientId(Long clientId);
    List<Facture> findByStatut(String statut);
    Optional<Facture> findByNumeroFacture(String numeroFacture);
}
