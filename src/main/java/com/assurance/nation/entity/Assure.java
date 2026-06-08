package com.assurance.nation.entity;

import com.assurance.nation.entity.enums.UserType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "assures")
@PrimaryKeyJoinColumn(name = "id")
@DiscriminatorValue("ASSURE")
@Getter
@Setter
@NoArgsConstructor
public class Assure extends User {

    @Column(name = "num_securite_sociale", nullable = false, unique = true, length = 20)
    private String numSecuriteSociale;

    @Column(name = "date_affiliation")
    private LocalDate dateAffiliation;

    @Column(length = 150)
    private String emploi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medecin_traitant_id")
    private Medecin medecinTraitant;

    @Column(name = "est_actif")
    private boolean estActif = true;

    @OneToMany(mappedBy = "assure", fetch = FetchType.LAZY)
    private List<Consultation> consultations = new ArrayList<>();

    @OneToMany(mappedBy = "assure", fetch = FetchType.LAZY)
    private List<MedicalRecord> medicalRecords = new ArrayList<>();

    @PrePersist
    @PreUpdate
    void setType() {
        setUserType(UserType.ASSURE);
    }
}
