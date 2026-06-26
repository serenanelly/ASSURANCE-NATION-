package com.assurance.nation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Feuille de maladie — générée automatiquement à la création d'une consultation.
 */
@Entity
@Table(name = "medical_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted_at IS NULL")
public class MedicalRecord extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assure_id", nullable = false)
    private Assure assure;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medecin_id", nullable = false)
    private Medecin medecin;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "consultation_id", nullable = false, unique = true)
    private Consultation consultation;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "nom_maladie", length = 255)
    private String nomMaladie;

    @Column(name = "est_remboursee")
    private boolean estRemboursee;

    @Column(name = "date_remboursement")
    private LocalDateTime dateRemboursement;

    @Column(name = "montant_rembourse", precision = 12, scale = 2)
    private BigDecimal montantRembourse;
}
