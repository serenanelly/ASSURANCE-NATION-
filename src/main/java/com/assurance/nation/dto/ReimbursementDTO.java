package com.assurance.nation.dto;

import com.assurance.nation.entity.enums.ModePaiement;
import com.assurance.nation.entity.enums.ReimbursementStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ReimbursementDTO {

    private ReimbursementDTO() {}

    @Data
    public static class CreateRequest {
        @NotNull private UUID medicalRecordId;
        @NotNull @DecimalMin("0.01")
        private BigDecimal montantTotal;
        private ModePaiement modePaiement;
        private String notes;
    }

    @Data
    public static class RejectRequest {
        @NotBlank
        private String motif;
    }

    @Data
    public static class Response {
        private UUID id;
        private String numRemboursement;
        private UUID medicalRecordId;
        private UUID assureId;
        private String assureNom;
        private BigDecimal montantTotal;
        private Integer tauxRemboursement;
        private BigDecimal montantRembourse;
        private ModePaiement modePaiement;
        private ReimbursementStatus status;
        private LocalDateTime dateRemboursement;
        private LocalDateTime createdAt;
        private String justificatifUrl;
        private String notes;
    }

    @Data
    public static class ListStatistics {
        private BigDecimal totalRembourses;
        private long nombreRemboursements;
        private BigDecimal moyenneParRemboursement;
    }

    @Data
    public static class PageResponse {
        private List<Response> content;
        private long totalElements;
        private int totalPages;
        private int page;
        private int size;
        private ListStatistics statistics;
    }

    @Data
    public static class MonthlyStat {
        private String mois;
        private BigDecimal montant;
        private long count;
    }

    @Data
    public static class SpecialiteStat {
        private BigDecimal montant;
        private double taux;
    }

    @Data
    public static class DashboardResponse {
        private long totalRemboursements;
        private BigDecimal totalMontants;
        private long pendingCount;
        private long approvedCount;
        private long rejectedCount;
        private long paidCount;
        private BigDecimal montantTotalPaye;
        private List<MonthlyStat> rembourseParMois;
        private Map<String, SpecialiteStat> rembourseParSpecialite;
    }
}
