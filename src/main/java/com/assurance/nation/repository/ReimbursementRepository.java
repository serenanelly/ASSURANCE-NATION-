package com.assurance.nation.repository;

import com.assurance.nation.entity.Reimbursement;
import com.assurance.nation.entity.enums.ReimbursementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReimbursementRepository extends JpaRepository<Reimbursement, UUID> {

    Optional<Reimbursement> findByNumRemboursement(String numRemboursement);

    boolean existsByMedicalRecordId(UUID medicalRecordId);

    long countByStatus(ReimbursementStatus status);

    @Query("SELECT COALESCE(SUM(r.montantRembourse), 0) FROM Reimbursement r WHERE r.status = 'PAID'")
    BigDecimal sumMontantPaye();

    @Query(value = "SELECT COUNT(*) FROM reimbursements WHERE num_remboursement LIKE :prefix || '%'", nativeQuery = true)
    long countByNumPrefix(String prefix);

    @Query("""
            SELECT r FROM Reimbursement r
            JOIN r.medicalRecord mr
            WHERE (:status IS NULL OR r.status = :status)
            AND (:assureId IS NULL OR mr.assure.id = :assureId)
            AND (:startDate IS NULL OR r.createdAt >= :startDate)
            AND (:endDate IS NULL OR r.createdAt <= :endDate)
            """)
    Page<Reimbursement> findFiltered(
            @Param("status") ReimbursementStatus status,
            @Param("assureId") UUID assureId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT r FROM Reimbursement r JOIN r.medicalRecord mr WHERE mr.assure.id = :assureId")
    Page<Reimbursement> findByAssureId(@Param("assureId") UUID assureId, Pageable pageable);

    List<Reimbursement> findByStatus(ReimbursementStatus status);
}
