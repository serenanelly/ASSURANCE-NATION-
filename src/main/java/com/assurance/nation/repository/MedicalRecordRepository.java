package com.assurance.nation.repository;

import com.assurance.nation.entity.MedicalRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, UUID> {

    List<MedicalRecord> findByAssureIdOrderByDateDesc(UUID assureId);

    Optional<MedicalRecord> findByConsultationId(UUID consultationId);

    boolean existsByConsultationId(UUID consultationId);

    @Query("""
            SELECT mr FROM MedicalRecord mr
            WHERE mr.assure.id = :assureId
            AND (:startDate IS NULL OR mr.date >= :startDate)
            AND (:endDate IS NULL OR mr.date <= :endDate)
            AND (:remboursee IS NULL OR mr.estRemboursee = :remboursee)
            """)
    Page<MedicalRecord> findByAssureFiltered(
            @Param("assureId") UUID assureId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("remboursee") Boolean remboursee,
            Pageable pageable);
}
