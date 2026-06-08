package com.assurance.nation.integration;

import com.assurance.nation.AbstractIntegrationTest;
import com.assurance.nation.dto.*;
import com.assurance.nation.entity.enums.PrescriptionType;
import com.assurance.nation.entity.enums.Specialite;
import com.assurance.nation.entity.enums.TypeConsultation;
import com.assurance.nation.entity.enums.UserType;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Parcours complet : médecin → assuré → consultation → prescription → remboursement.
 */
class FullFlowIT extends AbstractIntegrationTest {

    @Test
    void fullMedicalFlow() throws Exception {
        String adminToken = loginAsAdmin();
        String suffix = String.valueOf(System.nanoTime());

        MedecinDTO.RegisterMedecinRequest medReq = new MedecinDTO.RegisterMedecinRequest();
        medReq.setEmail("dr." + suffix + "@test.com");
        medReq.setPassword("Secure1!");
        medReq.setNom("Martin");
        medReq.setPrenom("Paul");
        medReq.setNumeroRPPS("9876543210" + suffix.substring(suffix.length() - 1));
        medReq.setSpecialite(Specialite.GENERALISTE);
        String medJson = mockMvc.perform(post("/api/v1/users/medecins")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID medecinId = UUID.fromString(objectMapper.readTree(medJson).get("id").asText());

        AssureDTO.RegisterAssureRequest assReq = new AssureDTO.RegisterAssureRequest();
        assReq.setEmail("patient." + suffix + "@test.com");
        assReq.setPassword("Secure1!");
        assReq.setNom("Durand");
        assReq.setPrenom("Marie");
        assReq.setNumSecuriteSociale("12345678901" + suffix.substring(suffix.length() - 4));
        assReq.setMedecinTraitantId(medecinId);
        String assJson = mockMvc.perform(post("/api/v1/users/assures")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID assureId = UUID.fromString(objectMapper.readTree(assJson).get("id").asText());

        AuthDTO.LoginRequest medLogin = new AuthDTO.LoginRequest();
        medLogin.setEmail(medReq.getEmail());
        medLogin.setPassword(medReq.getPassword());
        String medToken = objectMapper.readTree(mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medLogin)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()).get("accessToken").asText();

        ConsultationDTO.CreateRequest consReq = new ConsultationDTO.CreateRequest();
        consReq.setAssureId(assureId);
        consReq.setDateConsultation(LocalDateTime.now().plusDays(1));
        consReq.setTypeConsultation(TypeConsultation.GENERALISTE);
        consReq.setDiagnostique("Grippe");
        String consJson = mockMvc.perform(post("/api/v1/consultations")
                        .header("Authorization", "Bearer " + medToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID consultationId = UUID.fromString(objectMapper.readTree(consJson).get("id").asText());
        UUID medicalRecordId = UUID.fromString(objectMapper.readTree(consJson).get("medicalRecordId").asText());

        PrescriptionDTO.CreateRequest prescReq = new PrescriptionDTO.CreateRequest();
        prescReq.setType(PrescriptionType.MEDICAMENT);
        prescReq.setMedicament("Ibuprofène");
        mockMvc.perform(post("/api/v1/consultations/" + consultationId + "/prescriptions")
                        .header("Authorization", "Bearer " + medToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(prescReq)))
                .andExpect(status().isCreated());

        ReimbursementDTO.CreateRequest reimbReq = new ReimbursementDTO.CreateRequest();
        reimbReq.setMedicalRecordId(medicalRecordId);
        reimbReq.setMontantTotal(new BigDecimal("75.00"));
        String reimbJson = mockMvc.perform(post("/api/v1/reimbursements")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reimbReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tauxRemboursement").value(100))
                .andExpect(jsonPath("$.montantRembourse").value(75.0))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();
        UUID reimbursementId = UUID.fromString(objectMapper.readTree(reimbJson).get("id").asText());

        mockMvc.perform(patch("/api/v1/reimbursements/" + reimbursementId + "/approve")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        mockMvc.perform(patch("/api/v1/reimbursements/" + reimbursementId + "/mark-paid")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        mockMvc.perform(get("/api/v1/medical-records/" + assureId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
