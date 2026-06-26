package com.assurance.nation.service;

import com.assurance.nation.dto.PrescriptionDTO;
import com.assurance.nation.entity.Assure;
import com.assurance.nation.entity.Consultation;
import com.assurance.nation.entity.PrescriptionMedicament;
import com.assurance.nation.entity.enums.PrescriptionType;
import com.assurance.nation.mapper.PrescriptionMapper;
import com.assurance.nation.repository.PrescriptionRepository;
import com.assurance.nation.security.OwnershipService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PrescriptionServiceTest {

    @Mock private PrescriptionRepository prescriptionRepository;
    @Mock private ConsultationService consultationService;
    @Mock private MedecinService medecinService;
    @Mock private PrescriptionMapper prescriptionMapper;
    @Mock private AuditService auditService;
    @Mock private OwnershipService ownershipService;
    @Mock private NotificationService notificationService;
    @InjectMocks private PrescriptionService prescriptionService;

    @BeforeEach
    void security() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("m@test.com", "p", java.util.List.of()));
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createMedicamentPrescription() {
        UUID consultationId = UUID.randomUUID();
        Consultation consultation = Consultation.builder().id(consultationId).build();
        when(consultationService.getEntity(consultationId)).thenReturn(consultation);
        doNothing().when(ownershipService).assertCanAccessConsultation(consultation);
        when(prescriptionRepository.save(any())).thenAnswer(inv -> {
            PrescriptionMedicament p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });
        when(prescriptionMapper.toResponse(any())).thenReturn(new PrescriptionDTO.Response());

        PrescriptionDTO.CreateRequest req = new PrescriptionDTO.CreateRequest();
        req.setType(PrescriptionType.MEDICAMENT);
        req.setMedicament("Paracétamol");
        assertThat(prescriptionService.create(consultationId, req, "127.0.0.1")).isNotNull();
        verify(notificationService).notifyPrescriptionAdded(any());
    }

    @Test
    void createSpecialistPrescription() {
        UUID consultationId = UUID.randomUUID();
        Consultation consultation = Consultation.builder().id(consultationId).build();
        when(consultationService.getEntity(consultationId)).thenReturn(consultation);
        doNothing().when(ownershipService).assertCanAccessConsultation(consultation);
        when(prescriptionRepository.save(any())).thenAnswer(inv -> {
            com.assurance.nation.entity.PrescriptionConsultation pc = inv.getArgument(0);
            pc.setId(UUID.randomUUID());
            return pc;
        });
        when(prescriptionMapper.toResponse(any())).thenReturn(new PrescriptionDTO.Response());

        PrescriptionDTO.CreateRequest req = new PrescriptionDTO.CreateRequest();
        req.setType(PrescriptionType.CONSULTATION_SPECIALISTE);
        req.setMotif("Douleur chronique");
        assertThat(prescriptionService.create(consultationId, req, "127.0.0.1")).isNotNull();
    }

    @Test
    void findById_ok() {
        UUID id = UUID.randomUUID();
        PrescriptionMedicament p = new PrescriptionMedicament();
        p.setId(id);
        p.setConsultation(Consultation.builder().id(UUID.randomUUID()).build());
        when(prescriptionRepository.findById(id)).thenReturn(Optional.of(p));
        doNothing().when(ownershipService).assertCanAccessConsultation(any());
        when(prescriptionMapper.toResponse(p)).thenReturn(new PrescriptionDTO.Response());
        assertThat(prescriptionService.findById(id)).isNotNull();
    }

    @Test
    void findByConsultationId_ok() {
        UUID consultationId = UUID.randomUUID();
        Consultation c = Consultation.builder().id(consultationId).build();
        when(consultationService.getEntity(consultationId)).thenReturn(c);
        doNothing().when(ownershipService).assertCanAccessConsultation(c);
        when(prescriptionRepository.findByConsultationId(consultationId)).thenReturn(List.of());
        assertThat(prescriptionService.findByConsultationId(consultationId)).isEmpty();
    }

    @Test
    void delete_softDeletes() {
        UUID id = UUID.randomUUID();
        PrescriptionMedicament p = new PrescriptionMedicament();
        p.setId(id);
        p.setConsultation(Consultation.builder().id(UUID.randomUUID()).build());
        when(prescriptionRepository.findById(id)).thenReturn(Optional.of(p));
        doNothing().when(ownershipService).assertCanAccessConsultation(any());
        when(prescriptionRepository.save(any())).thenReturn(p);
        prescriptionService.delete(id, "erreur dosage", "127.0.0.1");
        assertThat(p.isDeleted()).isTrue();
    }
}
