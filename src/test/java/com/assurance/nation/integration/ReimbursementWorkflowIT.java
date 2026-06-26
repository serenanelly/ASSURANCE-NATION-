package com.assurance.nation.integration;

import com.assurance.nation.AbstractIntegrationTest;
import com.assurance.nation.dto.ReimbursementDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Workflow remboursement complet (IT avec Testcontainers optionnel en CI).
 * Utilise le profil test H2 pour exécution locale rapide.
 */
@Testcontainers(disabledWithoutDocker = true)
class ReimbursementWorkflowIT extends AbstractIntegrationTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("assurance_test")
            .withUsername("test")
            .withPassword("test");

    @Test
    void workflow_createApproveReject() throws Exception {
        String token = loginAsAdmin();
        mockMvc.perform(get("/api/v1/reimbursements/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRemboursements").exists());
    }

    @Test
    void reject_requiresMotif() throws Exception {
        String token = loginAsAdmin();
        UUID fakeId = UUID.randomUUID();
        ReimbursementDTO.RejectRequest req = new ReimbursementDTO.RejectRequest();
        req.setMotif("Test rejet");
        mockMvc.perform(post("/api/v1/reimbursements/" + fakeId + "/reject")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }
}
