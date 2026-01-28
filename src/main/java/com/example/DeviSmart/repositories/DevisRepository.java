package com.example.DeviSmart.repositories;

import com.example.DeviSmart.entities.Devis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DevisRepository extends JpaRepository<Devis, Long> {
    List<Devis> findByClientId(Long clientId);
    List<Devis> findByStatut(String statut);
}
