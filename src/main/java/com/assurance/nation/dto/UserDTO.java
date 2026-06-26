package com.assurance.nation.dto;

import com.assurance.nation.entity.enums.Sexe;
import com.assurance.nation.entity.enums.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class UserDTO {

    private UserDTO() {}

    @Data
    public static class UserResponse {
        private UUID id;
        private String email;
        private String nom;
        private String prenom;
        private LocalDate dateNaissance;
        private String lieuNaissance;
        private String adresse;
        private String telephone;
        private Sexe sexe;
        private UserType userType;
        private Set<String> roles;
        private String photoUrl;
        private LocalDateTime createdAt;
    }

    @Data
    public static class UpdateUserRequest {
        @Email
        private String email;
        private String nom;
        private String prenom;
        private LocalDate dateNaissance;
        private String lieuNaissance;
        private String adresse;
        private String telephone;
        private Sexe sexe;
        private String photoUrl;
    }

    @Data
    public static class ChangePasswordRequest {
        @NotBlank
        private String currentPassword;
        @NotBlank
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")
        private String newPassword;
        @NotBlank
        private String confirmPassword;
    }

    @Data
    public static class PageResponse<T> {
        private List<T> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
    }
}
