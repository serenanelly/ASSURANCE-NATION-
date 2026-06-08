package com.assurance.nation.controller;

import com.assurance.nation.AbstractIntegrationTest;
import com.assurance.nation.dto.AssureDTO;
import com.assurance.nation.dto.MedecinDTO;
import com.assurance.nation.entity.enums.Specialite;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AssureControllerIT extends AbstractIntegrationTest {

    @Test
    void registerAndSearchByNss() throws Exception {
        String adminToken = loginAsAdmin();
        String suffix = String.valueOf(System.nanoTime());
        String nss = "45678901234" + suffix.substring(suffix.length() - 4);

        MedecinDTO.RegisterMedecinRequest medReq = new MedecinDTO.RegisterMedecinRequest();
        medReq.setEmail("traitant." + suffix + "@test.com");
        medReq.setPassword("Secure1!");
        medReq.setNom("Moreau");
        medReq.setPrenom("Claire");
        medReq.setNumeroRPPS("8889990000" + suffix.substring(suffix.length() - 1));
        medReq.setSpecialite(Specialite.GENERALISTE);
        String medJson = mockMvc.perform(post("/api/v1/users/medecins")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID medecinId = UUID.fromString(objectMapper.readTree(medJson).get("id").asText());

        AssureDTO.RegisterAssureRequest assReq = new AssureDTO.RegisterAssureRequest();
        assReq.setEmail("assure." + suffix + "@test.com");
        assReq.setPassword("Secure1!");
        assReq.setNom("Garnier");
        assReq.setPrenom("Julie");
        assReq.setNumSecuriteSociale(nss);
        assReq.setMedecinTraitantId(medecinId);

        mockMvc.perform(post("/api/v1/users/assures")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numSecuriteSociale").value(nss));

        mockMvc.perform(get("/api/v1/users/assures/search")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("nss", nss))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numSecuriteSociale").value(nss))
                .andExpect(jsonPath("$.nom").value("Garnier"));
    }
}
