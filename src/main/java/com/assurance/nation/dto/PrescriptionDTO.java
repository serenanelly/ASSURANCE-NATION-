package com.assurance.nation.dto;

import com.assurance.nation.entity.enums.PrescriptionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

public class PrescriptionDTO {

    private PrescriptionDTO() {}

    @Data
    public static class CreateRequest {
        @NotNull private PrescriptionType type;
        private String medicament;
        private String posologie;
        private String duree;
        private String notes;
        private UUID medecinSpecialisteId;
        private String motif;
        private String priorite;
        private String codeReference;
    }

    @Data
    public static class DeleteRequest {
        private String motifSuppression;
    }

    @Data
    public static class Response {
        private UUID id;
        private UUID consultationId;
        private PrescriptionType type;
        private String medicament;
        private String posologie;
        private String duree;
        private String notes;
        private UUID medecinSpecialisteId;
        private String motif;
        private String priorite;
        private String codeReference;
    }
}
