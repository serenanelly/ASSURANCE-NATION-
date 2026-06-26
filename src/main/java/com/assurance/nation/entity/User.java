package com.assurance.nation.entity;

import com.assurance.nation.entity.enums.Sexe;
import com.assurance.nation.entity.enums.UserType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Supertype utilisateur (héritage JOINED : Médecin, Assuré, ou Assureur sur table users).
 */
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted_at IS NULL")
public abstract class User extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Column(name = "lieu_naissance", length = 150)
    private String lieuNaissance;

    @Column(length = 500)
    private String adresse;

    @Column(length = 30)
    private String telephone;

    @Enumerated(EnumType.STRING)
    @Column(length = 1)
    private Sexe sexe;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", insertable = false, updatable = false, length = 20)
    private UserType userType;

    /** Photo de profil (data URL base64 ou URL). */
    @Column(name = "photo_url", columnDefinition = "TEXT")
    private String photoUrl;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();
}
