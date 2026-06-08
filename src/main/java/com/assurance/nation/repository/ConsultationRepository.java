package com.assurance.nation.repository;

import com.assurance.nation.entity.Consultation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, UUID> {

    Page<Consultation> findByAssureId(UUID assureId, Pageable pageable);

    Page<Consultation> findByMedecinId(UUID medecinId, Pageable pageable);

    @Query("SELECT c FROM Consultation c WHERE c.medecin.id = :medecinId OR c.assure.id = :assureId")
    Page<Consultation> findForMedecinOrAssure(UUID medecinId, UUID assureId, Pageable pageable);

    List<Consultation> findByAssureIdOrderByDateConsultationDesc(UUID assureId);

    boolean existsByMedecinIdAndAssureId(UUID medecinId, UUID assureId);
}
