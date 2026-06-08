package com.assurance.nation.entity;

import com.assurance.nation.entity.enums.TypeConsultation;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "consultations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLRestriction("deleted_at IS NULL")
public class Consultation extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assure_id", nullable = false)
    private Assure assure;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medecin_id", nullable = false)
    private Medecin medecin;

    @Column(name = "date_consultation", nullable = false)
    private LocalDateTime dateConsultation;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_consultation", nullable = false, length = 20)
    private TypeConsultation typeConsultation;

    @Column(columnDefinition = "TEXT")
    private String diagnostique;

    @Column(columnDefinition = "TEXT")
    private String motif;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "consultation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Prescription> prescriptions = new ArrayList<>();

    @OneToOne(mappedBy = "consultation", fetch = FetchType.LAZY)
    private MedicalRecord medicalRecord;
}
