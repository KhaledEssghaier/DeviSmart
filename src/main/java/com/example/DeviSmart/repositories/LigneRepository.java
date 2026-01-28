package com.example.DeviSmart.repositories;

import com.example.DeviSmart.entities.Ligne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LigneRepository extends JpaRepository<Ligne, Long> {
    List<Ligne> findByDevisId(Long devisId);
    List<Ligne> findByFactureId(Long factureId);
}
