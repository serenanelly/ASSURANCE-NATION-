package com.assurance.nation.dto;

import com.assurance.nation.entity.enums.Sexe;
import com.assurance.nation.entity.enums.Specialite;
import com.assurance.nation.entity.enums.UserType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

public class AuthDTO {

    private AuthDTO() {}

    @Data
    public static class RegisterRequest {
        @NotBlank @Email
        private String email;
        @NotBlank
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "Mot de passe invalide (8+ car., majuscule, chiffre, spécial)")
        private String password;
        private String confirmPassword;
        @NotBlank private String nom;
        @NotBlank private String prenom;
        private LocalDate dateNaissance;
        private String lieuNaissance;
        private String adresse;
        private String telephone;
        private Sexe sexe;
        @NotNull private UserType userType;
        private String numeroRPPS;
        private Specialite specialite;
        private String specialiteLibelle;
        private String numSecuriteSociale;
        private LocalDate dateAffiliation;
        private String emploi;
        private String photoUrl;
    }

    @Data
    public static class LoginRequest {
        @NotBlank @Email
        private String email;
        @NotBlank
        private String password;
    }

    @Data
    public static class RefreshRequest {
        @NotBlank
        private String refreshToken;
    }

    @Data
    public static class ValidateTokenRequest {
        @NotBlank
        private String token;
    }

    @Data
    public static class AuthResponse {
        @JsonProperty("accessToken")
        private String accessToken;
        /** Alias rétrocompatible spec frontend */
        @JsonProperty("token")
        public String getToken() {
            return accessToken;
        }
        private String refreshToken;
        private String tokenType = "Bearer";
        private long expiresIn;
        private UserDTO.UserResponse user;
        private String message;
    }

    @Data
    public static class TokenResponse {
        @JsonProperty("accessToken")
        private String accessToken;
        @JsonProperty("token")
        public String getToken() {
            return accessToken;
        }
        private String refreshToken;
        private String tokenType = "Bearer";
        private long expiresIn;
    }

    @Data
    public static class ValidateTokenResponse {
        private boolean valid;
        private String email;
        private String message;
    }
}
