package com.assurance.nation.controller;

import com.assurance.nation.AbstractIntegrationTest;
import com.assurance.nation.dto.AuthDTO;
import com.assurance.nation.entity.enums.Specialite;
import com.assurance.nation.entity.enums.UserType;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerIT extends AbstractIntegrationTest {

    @Test
    void register_medecin_success() throws Exception {
        AuthDTO.RegisterRequest req = new AuthDTO.RegisterRequest();
        req.setEmail("dr.unique" + System.nanoTime() + "@test.com");
        req.setPassword("Secure1!");
        req.setNom("Dupont");
        req.setPrenom("Jean");
        req.setUserType(UserType.MEDECIN);
        req.setNumeroRPPS("12345678901234");
        req.setSpecialite(Specialite.GENERALISTE);
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    void refresh_requiresValidToken() throws Exception {
        String token = loginAsAdmin();
        AuthDTO.RefreshRequest refresh = new AuthDTO.RefreshRequest();
        refresh.setRefreshToken("invalid");
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refresh)))
                .andExpect(status().isUnauthorized());
    }
}
