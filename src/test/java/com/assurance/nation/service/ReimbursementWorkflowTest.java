package com.assurance.nation.service;

import com.assurance.nation.dto.ReimbursementDTO;
import com.assurance.nation.entity.*;
import com.assurance.nation.entity.enums.ReimbursementStatus;
import com.assurance.nation.entity.enums.TypeConsultation;
import com.assurance.nation.exception.BusinessException;
import com.assurance.nation.mapper.ReimbursementMapper;
import com.assurance.nation.repository.MedicalRecordRepository;
import com.assurance.nation.repository.ReimbursementRepository;
import com.assurance.nation.security.OwnershipService;
import com.assurance.nation.util.PdfGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReimbursementWorkflowTest {

    @Mock private ReimbursementRepository reimbursementRepository;
    @Mock private MedicalRecordService medicalRecordService;
    @Mock private MedicalRecordRepository medicalRecordRepository;
    @Mock private ReimbursementMapper reimbursementMapper;
    @Mock private NotificationService notificationService;
    @Mock private AuditService auditService;
    @Mock private PdfGenerator pdfGenerator;
    @Mock private OwnershipService ownershipService;
    @InjectMocks private ReimbursementService reimbursementService;

    private Reimbursement reimbursement;
    private UUID reimbursementId;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("assureur@test.com", "x", java.util.List.of()));
        reimbursementId = UUID.randomUUID();
        Assure assure = new Assure();
        assure.setEmail("p@test.com");
        Consultation consultation = Consultation.builder().typeConsultation(TypeConsultation.GENERALISTE).build();
        MedicalRecord record = MedicalRecord.builder().assure(assure).consultation(consultation).estRemboursee(false).build();
        reimbursement = Reimbursement.builder()
                .id(reimbursementId)
                .numRemboursement("RB-2026-000001")
                .medicalRecord(record)
                .montantTotal(new BigDecimal("100"))
                .montantRembourse(new BigDecimal("100"))
                .tauxRemboursement(100)
                .status(ReimbursementStatus.PENDING)
                .build();
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void approve_pendingToApproved() throws Exception {
        when(reimbursementRepository.findById(reimbursementId)).thenReturn(Optional.of(reimbursement));
        when(pdfGenerator.generateJustificatif(any())).thenReturn("/tmp/test.pdf");
        when(reimbursementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(reimbursementMapper.toResponse(any())).thenReturn(new ReimbursementDTO.Response());

        ReimbursementDTO.Response result = reimbursementService.approve(reimbursementId, "127.0.0.1");
        assertThat(result).isNotNull();
        assertThat(reimbursement.getStatus()).isEqualTo(ReimbursementStatus.APPROVED);
        verify(notificationService).notifyReimbursementApproved(any(), any());
    }

    @Test
    void reject_pendingToRejected() {
        when(reimbursementRepository.findById(reimbursementId)).thenReturn(Optional.of(reimbursement));
        when(reimbursementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(reimbursementMapper.toResponse(any())).thenReturn(new ReimbursementDTO.Response());

        ReimbursementDTO.RejectRequest req = new ReimbursementDTO.RejectRequest();
        req.setMotif("Dossier incomplet");
        reimbursementService.reject(reimbursementId, req, "127.0.0.1");
        assertThat(reimbursement.getStatus()).isEqualTo(ReimbursementStatus.REJECTED);
    }

    @Test
    void markPaid_requiresApproved() {
        reimbursement.setStatus(ReimbursementStatus.PENDING);
        when(reimbursementRepository.findById(reimbursementId)).thenReturn(Optional.of(reimbursement));
        assertThatThrownBy(() -> reimbursementService.markPaid(reimbursementId, "127.0.0.1"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void markPaid_approvedToPaid() {
        reimbursement.setStatus(ReimbursementStatus.APPROVED);
        when(reimbursementRepository.findById(reimbursementId)).thenReturn(Optional.of(reimbursement));
        when(reimbursementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(reimbursementMapper.toResponse(any())).thenReturn(new ReimbursementDTO.Response());

        reimbursementService.markPaid(reimbursementId, "127.0.0.1");
        assertThat(reimbursement.getStatus()).isEqualTo(ReimbursementStatus.PAID);
        verify(medicalRecordRepository).save(any());
        verify(notificationService).notifyReimbursementPaid(any(), any());
    }
}
