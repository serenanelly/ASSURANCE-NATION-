package com.assurance.nation.entity;

import com.assurance.nation.entity.enums.PrescriptionType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "prescription_medicaments")
@PrimaryKeyJoinColumn(name = "id")
@DiscriminatorValue("MEDICAMENT")
@Getter
@Setter
@NoArgsConstructor
public class PrescriptionMedicament extends Prescription {

    @Column(nullable = false, length = 200)
    private String medicament;

    @Column(length = 500)
    private String posologie;

    @Column(length = 100)
    private String duree;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @PrePersist
    @PreUpdate
    void setType() {
        setType(PrescriptionType.MEDICAMENT);
    }
}
