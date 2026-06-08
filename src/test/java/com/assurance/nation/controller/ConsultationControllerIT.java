package com.assurance.nation.controller;

import com.assurance.nation.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ConsultationControllerIT extends AbstractIntegrationTest {

    @Test
    void listConsultations_withAdminToken() throws Exception {
        mockMvc.perform(get("/api/v1/consultations")
                        .header("Authorization", "Bearer " + loginAsAdmin()))
                .andExpect(status().isOk());
    }
}
