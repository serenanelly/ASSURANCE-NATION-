package com.assurance.nation.repository;

import com.assurance.nation.entity.Assure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AssureRepository extends JpaRepository<Assure, UUID> {
    Optional<Assure> findByNumSecuriteSociale(String numSecuriteSociale);
    boolean existsByNumSecuriteSociale(String numSecuriteSociale);

    @Query("""
            SELECT a FROM Assure a
            WHERE (:search IS NULL OR LOWER(a.nom) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(a.prenom) LIKE LOWER(CONCAT('%', :search, '%'))
                OR a.numSecuriteSociale LIKE CONCAT('%', :search, '%'))
            AND (:actif IS NULL OR a.estActif = :actif)
            """)
    Page<Assure> search(@Param("search") String search, @Param("actif") Boolean actif, Pageable pageable);
}
