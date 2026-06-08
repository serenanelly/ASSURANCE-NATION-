package com.assurance.nation.service;

import com.assurance.nation.dto.MedecinDTO;
import com.assurance.nation.entity.Medecin;
import com.assurance.nation.entity.enums.Specialite;
import com.assurance.nation.repository.MedecinRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedecinServiceTest {

    @Mock private MedecinRepository medecinRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RoleAssignmentService roleAssignmentService;
    @InjectMocks private MedecinService medecinService;

    @Test
    void findById_returnsMedecin() {
        UUID id = UUID.randomUUID();
        Medecin m = new Medecin();
        m.setId(id);
        m.setNom("Dupont");
        m.setPrenom("Jean");
        m.setNumeroRPPS("12345678901");
        m.setSpecialite(Specialite.GENERALISTE);
        when(medecinRepository.findById(id)).thenReturn(Optional.of(m));
        assertThat(medecinService.findById(id).getNumeroRPPS()).isEqualTo("12345678901");
    }

    @Test
    void registerMedecin_savesEntity() {
        MedecinDTO.RegisterMedecinRequest req = new MedecinDTO.RegisterMedecinRequest();
        req.setEmail("dr@test.com");
        req.setPassword("Secure1!");
        req.setNom("Martin");
        req.setPrenom("Paul");
        req.setNumeroRPPS("98765432109");
        req.setSpecialite(Specialite.SPECIALISTE);
        when(medecinRepository.existsByNumeroRPPS(req.getNumeroRPPS())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hash");
        when(medecinRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        assertThat(medecinService.register(req).getSpecialite()).isEqualTo(Specialite.SPECIALISTE);
    }

    @Test
    void findByRPPS_ok() {
        Medecin m = new Medecin();
        m.setNumeroRPPS("12345678901");
        when(medecinRepository.findByNumeroRPPS("12345678901")).thenReturn(Optional.of(m));
        assertThat(medecinService.findByRPPS("12345678901").getNumeroRPPS()).isEqualTo("12345678901");
    }

    @Test
    void update_ok() {
        UUID id = UUID.randomUUID();
        Medecin m = new Medecin();
        m.setId(id);
        when(medecinRepository.findById(id)).thenReturn(Optional.of(m));
        when(medecinRepository.save(any())).thenReturn(m);
        MedecinDTO.UpdateMedecinRequest req = new MedecinDTO.UpdateMedecinRequest();
        req.setNom("Nouveau");
        assertThat(medecinService.update(id, req).getNom()).isEqualTo("Nouveau");
    }

    @Test
    void delete_softDeletes() {
        UUID id = UUID.randomUUID();
        Medecin m = new Medecin();
        when(medecinRepository.findById(id)).thenReturn(Optional.of(m));
        when(medecinRepository.save(any())).thenReturn(m);
        medecinService.delete(id);
        assertThat(m.isDeleted()).isTrue();
    }

    @Test
    void findAll_paginated() {
        when(medecinRepository.search(any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new Medecin())));
        assertThat(medecinService.findAll(0, 20, "martin", Specialite.GENERALISTE).getContent()).hasSize(1);
    }
}
