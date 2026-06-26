package com.assurance.nation.controller;

import com.assurance.nation.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReimbursementControllerIT extends AbstractIntegrationTest {

    @Test
    void dashboard_accessibleWithToken() throws Exception {
        mockMvc.perform(get("/api/v1/reimbursements/dashboard")
                        .header("Authorization", "Bearer " + loginAsAdmin()))
                .andExpect(status().isOk());
    }

    @Test
    void listReimbursements() throws Exception {
        mockMvc.perform(get("/api/v1/reimbursements")
                        .header("Authorization", "Bearer " + loginAsAdmin()))
                .andExpect(status().isOk());
    }
}
