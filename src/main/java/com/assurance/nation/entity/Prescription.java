package com.assurance.nation.entity;

import com.assurance.nation.entity.enums.PrescriptionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

/**
 * Prescription abstraite (médicament ou consultation spécialiste).
 */
@Entity
@Table(name = "prescriptions")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "prescription_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLRestriction("deleted_at IS NULL")
public abstract class Prescription extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "consultation_id", nullable = false)
    private Consultation consultation;

    @Enumerated(EnumType.STRING)
    @Column(name = "prescription_type", insertable = false, updatable = false, length = 30)
    private PrescriptionType type;
}
