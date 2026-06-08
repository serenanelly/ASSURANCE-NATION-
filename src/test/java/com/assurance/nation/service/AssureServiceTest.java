package com.assurance.nation.service;

import com.assurance.nation.dto.AssureDTO;
import com.assurance.nation.entity.Assure;
import com.assurance.nation.entity.Medecin;
import com.assurance.nation.entity.enums.Specialite;
import com.assurance.nation.repository.AssureRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssureServiceTest {

    @Mock private AssureRepository assureRepository;
    @Mock private MedecinService medecinService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RoleAssignmentService roleAssignmentService;
    @InjectMocks private AssureService assureService;

    @Test
    void findById_ok() {
        UUID id = UUID.randomUUID();
        Assure a = new Assure();
        a.setId(id);
        a.setNumSecuriteSociale("123456789012345");
        when(assureRepository.findById(id)).thenReturn(Optional.of(a));
        assertThat(assureService.findById(id).getNumSecuriteSociale()).isEqualTo("123456789012345");
    }

    @Test
    void register_ok() {
        AssureDTO.RegisterAssureRequest req = new AssureDTO.RegisterAssureRequest();
        req.setEmail("p@test.com");
        req.setPassword("Secure1!");
        req.setNom("X");
        req.setPrenom("Y");
        req.setNumSecuriteSociale("123456789012345");
        when(assureRepository.existsByNumSecuriteSociale("123456789012345")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(assureRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertThat(assureService.register(req).getNumSecuriteSociale()).isEqualTo("123456789012345");
    }

    @Test
    void searchByNSS_ok() {
        Assure a = new Assure();
        a.setNumSecuriteSociale("123456789012345");
        when(assureRepository.findByNumSecuriteSociale("123456789012345")).thenReturn(Optional.of(a));
        assertThat(assureService.searchByNSS("123456789012345").getNumSecuriteSociale())
                .isEqualTo("123456789012345");
    }

    @Test
    void update_ok() {
        UUID id = UUID.randomUUID();
        Assure a = new Assure();
        a.setId(id);
        when(assureRepository.findById(id)).thenReturn(Optional.of(a));
        when(assureRepository.save(any())).thenReturn(a);
        AssureDTO.UpdateAssureRequest req = new AssureDTO.UpdateAssureRequest();
        req.setNom("Nouveau");
        assertThat(assureService.update(id, req).getNom()).isEqualTo("Nouveau");
    }

    @Test
    void delete_softDeletes() {
        UUID id = UUID.randomUUID();
        Assure a = new Assure();
        when(assureRepository.findById(id)).thenReturn(Optional.of(a));
        when(assureRepository.save(any())).thenReturn(a);
        assureService.delete(id);
        assertThat(a.isDeleted()).isTrue();
    }

    @Test
    void updateMedecinTraitant_generalisteOnly() {
        UUID id = UUID.randomUUID();
        UUID medecinId = UUID.randomUUID();
        Assure a = new Assure();
        Medecin m = new Medecin();
        m.setSpecialite(Specialite.GENERALISTE);
        when(assureRepository.findById(id)).thenReturn(Optional.of(a));
        when(medecinService.getEntity(medecinId)).thenReturn(m);
        when(assureRepository.save(any())).thenReturn(a);
        AssureDTO.UpdateMedecinTraitantRequest req = new AssureDTO.UpdateMedecinTraitantRequest();
        req.setMedecinTraitantId(medecinId);
        assureService.updateMedecinTraitant(id, req);
        verify(assureRepository).save(a);
    }

    @Test
    void findAll_paginated() {
        when(assureRepository.search(any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new Assure())));
        assertThat(assureService.findAll(0, 20, null, null).getContent()).hasSize(1);
    }
}
