package com.assurance.nation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class MedicalRecordDTO {

    private MedicalRecordDTO() {}

    @Data
    public static class UpdateRequest {
        @NotBlank
        private String nomMaladie;
        private LocalDate date;
    }

    @Data
    public static class Response {
        private UUID id;
        private UUID assureId;
        private UUID medecinId;
        private UUID consultationId;
        private LocalDate date;
        private String nomMaladie;
        private boolean estRemboursee;
        private LocalDateTime dateRemboursement;
        private BigDecimal montantRembourse;
    }
}
