package com.assurance.nation.security;

import com.assurance.nation.entity.Assure;
import com.assurance.nation.entity.Consultation;
import com.assurance.nation.entity.Medecin;
import com.assurance.nation.exception.UnauthorizedException;
import com.assurance.nation.repository.ConsultationRepository;
import com.assurance.nation.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OwnershipServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private ConsultationRepository consultationRepository;
    @Mock private RoleChecker roleChecker;
    @InjectMocks private OwnershipService ownershipService;

    @BeforeEach
    void auth() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("patient@test.com", "x",
                        List.of(new SimpleGrantedAuthority("ROLE_PATIENT"))));
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void patientCanAccessOwnAssure() {
        UUID assureId = UUID.randomUUID();
        Assure assure = new Assure();
        assure.setId(assureId);
        when(roleChecker.isAdmin()).thenReturn(false);
        when(roleChecker.isAssureur()).thenReturn(false);
        when(roleChecker.isPatient()).thenReturn(true);
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(assure));
        assertThatCode(() -> ownershipService.assertCanAccessAssure(assureId)).doesNotThrowAnyException();
    }

    @Test
    void patientCannotAccessOtherAssure() {
        UUID assureId = UUID.randomUUID();
        Assure assure = new Assure();
        assure.setId(UUID.randomUUID());
        when(roleChecker.isPatient()).thenReturn(true);
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(assure));
        assertThatThrownBy(() -> ownershipService.assertCanAccessAssure(assureId))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void medecinCanAccessPatientWithConsultation() {
        UUID assureId = UUID.randomUUID();
        UUID medecinId = UUID.randomUUID();
        Medecin medecin = new Medecin();
        medecin.setId(medecinId);
        when(roleChecker.isMedecin()).thenReturn(true);
        when(roleChecker.isPatient()).thenReturn(false);
        when(roleChecker.isAdmin()).thenReturn(false);
        when(roleChecker.isAssureur()).thenReturn(false);
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(medecin));
        when(consultationRepository.existsByMedecinIdAndAssureId(medecinId, assureId)).thenReturn(true);
        assertThatCode(() -> ownershipService.assertCanAccessAssure(assureId)).doesNotThrowAnyException();
    }

    @Test
    void assertCanAccessConsultation_patientOwn() {
        Assure assure = new Assure();
        UUID assureId = UUID.randomUUID();
        assure.setId(assureId);
        Consultation c = Consultation.builder().assure(assure).build();
        when(roleChecker.isPatient()).thenReturn(true);
        when(userRepository.findByEmail("patient@test.com")).thenReturn(Optional.of(assure));
        assertThatCode(() -> ownershipService.assertCanAccessConsultation(c)).doesNotThrowAnyException();
    }
}
