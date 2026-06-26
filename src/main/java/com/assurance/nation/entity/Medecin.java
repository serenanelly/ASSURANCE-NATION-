package com.assurance.nation.entity;

import com.assurance.nation.entity.enums.Specialite;
import com.assurance.nation.entity.enums.UserType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "medecins")
@PrimaryKeyJoinColumn(name = "id")
@DiscriminatorValue("MEDECIN")
@Getter
@Setter
@NoArgsConstructor
public class Medecin extends User {

    @Column(name = "numero_rpps", nullable = false, unique = true, length = 20)
    private String numeroRPPS;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Specialite specialite;

    /** Libellé libre de la spécialité (ex. "Cardiologie") — renseigné pour les spécialistes. */
    @Column(name = "specialite_libelle", length = 150)
    private String specialiteLibelle;

    @Column(name = "est_assure")
    private boolean estAssure;

    @OneToMany(mappedBy = "medecin", fetch = FetchType.LAZY)
    private List<Consultation> consultations = new ArrayList<>();

    @OneToMany(mappedBy = "medecinTraitant", fetch = FetchType.LAZY)
    private List<Assure> assures = new ArrayList<>();

    @PrePersist
    @PreUpdate
    void setType() {
        setUserType(UserType.MEDECIN);
    }
}
