package com.assurance.nation.dto;

import com.assurance.nation.entity.enums.Specialite;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

public class MedecinDTO {

    private MedecinDTO() {}

    @Data
    public static class RegisterMedecinRequest {
        @NotBlank private String email;
        @NotBlank private String password;
        @NotBlank private String nom;
        @NotBlank private String prenom;
        @NotBlank private String numeroRPPS;
        @NotNull private Specialite specialite;
        private String specialiteLibelle;
        private String telephone;
        private boolean estAssure;
        private String photoUrl;
    }

    @Data
    public static class UpdateMedecinRequest {
        private String nom;
        private String prenom;
        private String telephone;
        private Specialite specialite;
        private String specialiteLibelle;
        private Boolean estAssure;
        private String photoUrl;
    }

    @Data
    public static class MedecinResponse {
        private UUID id;
        private String email;
        private String nom;
        private String prenom;
        private String numeroRPPS;
        private Specialite specialite;
        private String specialiteLibelle;
        private boolean estAssure;
        private String photoUrl;
    }
}
