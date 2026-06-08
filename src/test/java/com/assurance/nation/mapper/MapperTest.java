package com.assurance.nation.mapper;

import com.assurance.nation.entity.*;
import com.assurance.nation.entity.enums.Specialite;
import com.assurance.nation.entity.enums.TypeConsultation;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MapperTest {

    @Test
    void userMapper_mapsRoles() {
        Medecin m = new Medecin();
        m.setId(UUID.randomUUID());
        m.setEmail("dr@test.com");
        m.setNom("A");
        m.setPrenom("B");
        m.setNumeroRPPS("12345678901234");
        m.setSpecialite(Specialite.GENERALISTE);
        Role role = Role.builder().roleName(com.assurance.nation.entity.enums.RoleName.MEDECIN).build();
        m.setRoles(Set.of(role));
        assertThat(new UserMapper().toResponse(m).getRoles()).contains("ROLE_MEDECIN");
    }

    @Test
    void consultationMapper_mapsFields() {
        Assure a = new Assure();
        a.setId(UUID.randomUUID());
        a.setNom("Patient");
        a.setPrenom("Un");
        Medecin med = new Medecin();
        med.setId(UUID.randomUUID());
        med.setNom("Doc");
        med.setPrenom("Tor");
        Consultation c = Consultation.builder()
                .id(UUID.randomUUID())
                .assure(a)
                .medecin(med)
                .dateConsultation(LocalDateTime.now())
                .typeConsultation(TypeConsultation.GENERALISTE)
                .build();
        assertThat(new ConsultationMapper().toResponse(c).getAssureNom()).contains("Patient");
    }

    @Test
    void reimbursementMapper_mapsAmounts() {
        Reimbursement r = Reimbursement.builder()
                .id(UUID.randomUUID())
                .numRemboursement("RB-2026-000001")
                .montantTotal(new BigDecimal("50"))
                .tauxRemboursement(80)
                .montantRembourse(new BigDecimal("40"))
                .medicalRecord(MedicalRecord.builder().id(UUID.randomUUID()).build())
                .build();
        assertThat(new ReimbursementMapper().toResponse(r).getMontantRembourse())
                .isEqualByComparingTo("40");
    }
}
