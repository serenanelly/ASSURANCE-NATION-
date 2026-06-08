package com.assurance.nation.controller;

import com.assurance.nation.AbstractIntegrationTest;
import com.assurance.nation.dto.MedecinDTO;
import com.assurance.nation.entity.enums.Specialite;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MedecinControllerIT extends AbstractIntegrationTest {

    @Test
    void registerListAndDeleteMedecin() throws Exception {
        String adminToken = loginAsAdmin();
        String suffix = String.valueOf(System.nanoTime());

        MedecinDTO.RegisterMedecinRequest medReq = new MedecinDTO.RegisterMedecinRequest();
        medReq.setEmail("medecin." + suffix + "@test.com");
        medReq.setPassword("Secure1!");
        medReq.setNom("Leroy");
        medReq.setPrenom("Sophie");
        medReq.setNumeroRPPS("5556667770" + suffix.substring(suffix.length() - 1));
        medReq.setSpecialite(Specialite.SPECIALISTE);

        String medJson = mockMvc.perform(post("/api/v1/users/medecins")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroRPPS").value(medReq.getNumeroRPPS()))
                .andReturn().getResponse().getContentAsString();
        UUID medecinId = UUID.fromString(objectMapper.readTree(medJson).get("id").asText());

        mockMvc.perform(get("/api/v1/users/medecins")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("search", "Leroy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].nom").value("Leroy"));

        mockMvc.perform(delete("/api/v1/users/medecins/" + medecinId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }
}
