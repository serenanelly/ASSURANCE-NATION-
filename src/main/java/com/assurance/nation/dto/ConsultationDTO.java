package com.assurance.nation.dto;

import com.assurance.nation.entity.enums.TypeConsultation;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

public class ConsultationDTO {

    private ConsultationDTO() {}

    @Data
    public static class CreateRequest {
        @NotNull private UUID assureId;
        @NotNull private LocalDateTime dateConsultation;
        @NotNull private TypeConsultation typeConsultation;
        private String diagnostique;
        private String motif;
        private String notes;
    }

    @Data
    public static class UpdateRequest {
        private LocalDateTime dateConsultation;
        private TypeConsultation typeConsultation;
        private String diagnostique;
        private String motif;
        private String notes;
        private Boolean reschedule;
    }

    @Data
    public static class CancelRequest {
        private String motifAnnulation;
        private Boolean notifyPatient = true;
    }

    @Data
    public static class Response {
        private UUID id;
        private UUID assureId;
        private String assureNom;
        private UUID medecinId;
        private String medecinNom;
        private LocalDateTime dateConsultation;
        private TypeConsultation typeConsultation;
        private String diagnostique;
        private String motif;
        private String notes;
        private UUID medicalRecordId;
    }
}
