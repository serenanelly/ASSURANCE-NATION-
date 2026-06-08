package com.assurance.nation.repository;

import com.assurance.nation.entity.Medecin;
import com.assurance.nation.entity.enums.Specialite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedecinRepository extends JpaRepository<Medecin, UUID> {
    Optional<Medecin> findByNumeroRPPS(String numeroRPPS);
    boolean existsByNumeroRPPS(String numeroRPPS);

    @Query("""
            SELECT m FROM Medecin m
            WHERE (:search IS NULL OR LOWER(m.nom) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(m.prenom) LIKE LOWER(CONCAT('%', :search, '%'))
                OR m.numeroRPPS LIKE CONCAT('%', :search, '%'))
            AND (:specialite IS NULL OR m.specialite = :specialite)
            """)
    Page<Medecin> search(@Param("search") String search, @Param("specialite") Specialite specialite, Pageable pageable);
}
