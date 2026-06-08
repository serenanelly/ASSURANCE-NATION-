package com.assurance.nation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

public class AssureDTO {

    private AssureDTO() {}

    @Data
    public static class RegisterAssureRequest {
        @NotBlank private String email;
        @NotBlank private String password;
        @NotBlank private String nom;
        @NotBlank private String prenom;
        @NotBlank private String numSecuriteSociale;
        private LocalDate dateAffiliation;
        private String emploi;
        private UUID medecinTraitantId;
    }

    @Data
    public static class UpdateAssureRequest {
        private String nom;
        private String prenom;
        private String emploi;
        private Boolean estActif;
    }

    @Data
    public static class AssureResponse {
        private UUID id;
        private String email;
        private String nom;
        private String prenom;
        private String numSecuriteSociale;
        private LocalDate dateAffiliation;
        private String emploi;
        private UUID medecinTraitantId;
        private boolean estActif;
    }

    @Data
    public static class UpdateMedecinTraitantRequest {
        @NotNull
        private UUID medecinTraitantId;
    }
}
