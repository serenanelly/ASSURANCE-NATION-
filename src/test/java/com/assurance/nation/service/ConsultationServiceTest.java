package com.assurance.nation.service;

import com.assurance.nation.dto.ConsultationDTO;
import com.assurance.nation.entity.Assure;
import com.assurance.nation.entity.Consultation;
import com.assurance.nation.entity.Medecin;
import com.assurance.nation.entity.enums.TypeConsultation;
import com.assurance.nation.mapper.ConsultationMapper;
import com.assurance.nation.repository.ConsultationRepository;
import com.assurance.nation.repository.MedicalRecordRepository;
import com.assurance.nation.repository.UserRepository;
import com.assurance.nation.security.OwnershipService;
import com.assurance.nation.security.RoleChecker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultationServiceTest {

    @Mock private ConsultationRepository consultationRepository;
    @Mock private MedicalRecordRepository medicalRecordRepository;
    @Mock private AssureService assureService;
    @Mock private MedecinService medecinService;
    @Mock private UserRepository userRepository;
    @Mock private ConsultationMapper consultationMapper;
    @Mock private NotificationService notificationService;
    @Mock private AuditService auditService;
    @Mock private OwnershipService ownershipService;
    @Mock private RoleChecker roleChecker;
    @InjectMocks private ConsultationService consultationService;

    @BeforeEach
    void setUpSecurity() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("medecin@test.com", "x", java.util.List.of()));
    }

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createConsultation_createsMedicalRecord() {
        UUID assureId = UUID.randomUUID();
        Assure assure = new Assure();
        assure.setId(assureId);
        Medecin medecin = new Medecin();
        medecin.setId(UUID.randomUUID());
        when(userRepository.findByEmail("medecin@test.com")).thenReturn(Optional.of(medecin));
        when(assureService.getEntity(assureId)).thenReturn(assure);
        when(consultationRepository.save(any())).thenAnswer(i -> {
            Consultation c = i.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });
        when(medicalRecordRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(consultationMapper.toResponse(any())).thenReturn(new ConsultationDTO.Response());

        ConsultationDTO.CreateRequest req = new ConsultationDTO.CreateRequest();
        req.setAssureId(assureId);
        req.setDateConsultation(LocalDateTime.now());
        req.setTypeConsultation(TypeConsultation.GENERALISTE);
        req.setDiagnostique("Grippe");

        assertThat(consultationService.create(req, "127.0.0.1")).isNotNull();
        verify(medicalRecordRepository).save(any());
        verify(notificationService).notifyConsultationCreated(any());
    }

    @Test
    void findAll_asMedecin_filtersByMedecin() {
        Medecin medecin = new Medecin();
        medecin.setId(UUID.randomUUID());
        when(roleChecker.isMedecin()).thenReturn(true);
        when(ownershipService.getCurrentMedecin()).thenReturn(medecin);
        when(consultationRepository.findByMedecinId(eq(medecin.getId()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        assertThat(consultationService.findAll(0, 20)).isEmpty();
    }

    @Test
    void update_reschedulesNotifies() {
        UUID id = UUID.randomUUID();
        Consultation c = Consultation.builder().id(id).assure(new Assure()).build();
        when(consultationRepository.findById(id)).thenReturn(Optional.of(c));
        when(consultationRepository.save(any())).thenReturn(c);
        doNothing().when(ownershipService).assertCanAccessConsultation(c);
        when(consultationMapper.toResponse(c)).thenReturn(new ConsultationDTO.Response());

        ConsultationDTO.UpdateRequest req = new ConsultationDTO.UpdateRequest();
        req.setReschedule(true);
        req.setDateConsultation(LocalDateTime.now().plusDays(2));
        consultationService.update(id, req, "127.0.0.1");
        verify(notificationService).notifyConsultationRescheduled(c);
    }

    @Test
    void cancelConsultation_withMotif_skipsNotifyWhenDisabled() {
        UUID id = UUID.randomUUID();
        Consultation consultation = Consultation.builder().id(id).assure(new Assure()).build();
        when(consultationRepository.findById(id)).thenReturn(Optional.of(consultation));
        when(consultationRepository.save(any())).thenReturn(consultation);
        doNothing().when(ownershipService).assertCanAccessConsultation(consultation);

        ConsultationDTO.CancelRequest req = new ConsultationDTO.CancelRequest();
        req.setNotifyPatient(false);
        req.setMotifAnnulation("Patient absent");
        consultationService.cancel(id, req, "127.0.0.1");
        assertThat(consultation.isDeleted()).isTrue();
        verify(notificationService, never()).notifyConsultationCancelled(any());
    }

    @Test
    void findAll_asAdmin_returnsAll() {
        when(roleChecker.isMedecin()).thenReturn(false);
        when(roleChecker.isPatient()).thenReturn(false);
        when(consultationRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
        assertThat(consultationService.findAll(0, 20)).isEmpty();
    }

    @Test
    void findAll_asPatient_filtersByAssure() {
        Assure assure = new Assure();
        assure.setId(UUID.randomUUID());
        when(roleChecker.isMedecin()).thenReturn(false);
        when(roleChecker.isPatient()).thenReturn(true);
        when(ownershipService.getCurrentAssure()).thenReturn(assure);
        when(consultationRepository.findByAssureId(eq(assure.getId()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        assertThat(consultationService.findAll(0, 20)).isEmpty();
    }

    @Test
    void findById_returnsDto() {
        UUID id = UUID.randomUUID();
        Consultation c = Consultation.builder().id(id).build();
        when(consultationRepository.findById(id)).thenReturn(Optional.of(c));
        doNothing().when(ownershipService).assertCanAccessConsultation(c);
        when(consultationMapper.toResponse(c)).thenReturn(new ConsultationDTO.Response());
        assertThat(consultationService.findById(id)).isNotNull();
    }
}
