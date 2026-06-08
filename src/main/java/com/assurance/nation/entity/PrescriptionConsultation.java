package com.assurance.nation.entity;

import com.assurance.nation.entity.enums.PrescriptionType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "prescription_consultations")
@PrimaryKeyJoinColumn(name = "id")
@DiscriminatorValue("CONSULTATION_SPECIALISTE")
@Getter
@Setter
@NoArgsConstructor
public class PrescriptionConsultation extends Prescription {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medecin_specialiste_id")
    private Medecin medecinSpecialiste;

    @Column(columnDefinition = "TEXT")
    private String motif;

    @Column(length = 20)
    private String priorite;

    @Column(name = "code_reference", length = 50)
    private String codeReference;

    @PrePersist
    @PreUpdate
    void setType() {
        setType(PrescriptionType.CONSULTATION_SPECIALISTE);
    }
}
