package com.assurance.nation.integration;

import com.assurance.nation.AbstractIntegrationTest;
import com.assurance.nation.dto.*;
import com.assurance.nation.entity.enums.Specialite;
import com.assurance.nation.entity.enums.TypeConsultation;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RbacSecurityIT extends AbstractIntegrationTest {

    @Test
    void patientSeesOnlyOwnReimbursements_notAllData() throws Exception {
        String adminToken = loginAsAdmin();
        String suffix = String.valueOf(System.nanoTime());

        MedecinDTO.RegisterMedecinRequest medReq = new MedecinDTO.RegisterMedecinRequest();
        medReq.setEmail("dr.rbac." + suffix + "@test.com");
        medReq.setPassword("Secure1!");
        medReq.setNom("Roux");
        medReq.setPrenom("Marc");
        medReq.setNumeroRPPS("1212121212" + suffix.substring(suffix.length() - 1));
        medReq.setSpecialite(Specialite.GENERALISTE);
        String medJson = mockMvc.perform(post("/api/v1/users/medecins")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID medecinId = UUID.fromString(objectMapper.readTree(medJson).get("id").asText());

        AssureDTO.RegisterAssureRequest patientReq = new AssureDTO.RegisterAssureRequest();
        patientReq.setEmail("patient.rbac." + suffix + "@test.com");
        patientReq.setPassword("Secure1!");
        patientReq.setNom("Blanc");
        patientReq.setPrenom("Alice");
        patientReq.setNumSecuriteSociale("11111111111" + suffix.substring(suffix.length() - 4));
        patientReq.setMedecinTraitantId(medecinId);
        mockMvc.perform(post("/api/v1/users/assures")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patientReq)))
                .andExpect(status().isCreated());

        AssureDTO.RegisterAssureRequest otherReq = new AssureDTO.RegisterAssureRequest();
        otherReq.setEmail("other.rbac." + suffix + "@test.com");
        otherReq.setPassword("Secure1!");
        otherReq.setNom("Noir");
        otherReq.setPrenom("Bob");
        otherReq.setNumSecuriteSociale("22222222222" + suffix.substring(suffix.length() - 4));
        otherReq.setMedecinTraitantId(medecinId);
        String otherJson = mockMvc.perform(post("/api/v1/users/assures")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otherReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID otherAssureId = UUID.fromString(objectMapper.readTree(otherJson).get("id").asText());

        AuthDTO.LoginRequest medLogin = new AuthDTO.LoginRequest();
        medLogin.setEmail(medReq.getEmail());
        medLogin.setPassword(medReq.getPassword());
        String medToken = objectMapper.readTree(mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medLogin)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()).get("accessToken").asText();

        ConsultationDTO.CreateRequest consReq = new ConsultationDTO.CreateRequest();
        consReq.setAssureId(otherAssureId);
        consReq.setDateConsultation(LocalDateTime.now().plusDays(1));
        consReq.setTypeConsultation(TypeConsultation.GENERALISTE);
        consReq.setDiagnostique("Allergie");
        String consJson = mockMvc.perform(post("/api/v1/consultations")
                        .header("Authorization", "Bearer " + medToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID medicalRecordId = UUID.fromString(objectMapper.readTree(consJson).get("medicalRecordId").asText());

        ReimbursementDTO.CreateRequest reimbReq = new ReimbursementDTO.CreateRequest();
        reimbReq.setMedicalRecordId(medicalRecordId);
        reimbReq.setMontantTotal(new BigDecimal("80.00"));
        mockMvc.perform(post("/api/v1/reimbursements")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reimbReq)))
                .andExpect(status().isCreated());

        AuthDTO.LoginRequest patientLogin = new AuthDTO.LoginRequest();
        patientLogin.setEmail(patientReq.getEmail());
        patientLogin.setPassword(patientReq.getPassword());
        String patientToken = objectMapper.readTree(mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patientLogin)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()).get("accessToken").asText();

        mockMvc.perform(get("/api/v1/reimbursements")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));

        mockMvc.perform(get("/api/v1/reimbursements/dashboard")
                        .header("Authorization", "Bearer " + patientToken))
                .andExpect(status().isForbidden());
    }
}
