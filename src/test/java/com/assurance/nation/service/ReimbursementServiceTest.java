package com.assurance.nation.service;

import com.assurance.nation.dto.ReimbursementDTO;
import com.assurance.nation.entity.*;
import com.assurance.nation.entity.enums.TypeConsultation;
import com.assurance.nation.mapper.ReimbursementMapper;
import com.assurance.nation.repository.ReimbursementRepository;
import com.assurance.nation.security.OwnershipService;
import com.assurance.nation.util.Constants;
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

import com.assurance.nation.entity.Reimbursement;
import com.assurance.nation.entity.enums.ReimbursementStatus;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReimbursementServiceTest {

    @Mock private ReimbursementRepository reimbursementRepository;
    @Mock private MedicalRecordService medicalRecordService;
    @Mock private com.assurance.nation.repository.MedicalRecordRepository medicalRecordRepository;
    @Mock private OwnershipService ownershipService;
    @Mock private ReimbursementMapper reimbursementMapper;
    @Mock private NotificationService notificationService;
    @Mock private AuditService auditService;
    @Mock private PdfGenerator pdfGenerator;
    @InjectMocks private ReimbursementService reimbursementService;

    private MedicalRecord record;
    private UUID recordId;

    @BeforeEach
    void setUpSecurity() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("assureur@test.com", "x", java.util.List.of()));
    }

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @BeforeEach
    void setUp() {
        recordId = UUID.randomUUID();
        Assure assure = new Assure();
        assure.setEmail("patient@test.com");
        Medecin medecin = new Medecin();
        Consultation consultation = Consultation.builder()
                .typeConsultation(TypeConsultation.GENERALISTE)
                .build();
        record = MedicalRecord.builder()
                .id(recordId)
                .assure(assure)
                .medecin(medecin)
                .consultation(consultation)
                .build();
        consultation.setMedicalRecord(record);
    }

    @Test
    void createReimbursement_generaliste_100Percent() throws Exception {
        ReimbursementDTO.CreateRequest request = new ReimbursementDTO.CreateRequest();
        request.setMedicalRecordId(recordId);
        request.setMontantTotal(new BigDecimal("100.00"));

        when(medicalRecordService.getEntity(recordId)).thenReturn(record);
        when(reimbursementRepository.existsByMedicalRecordId(recordId)).thenReturn(false);
        when(reimbursementRepository.countByNumPrefix(any())).thenReturn(0L);
        when(pdfGenerator.generateJustificatif(any())).thenReturn("/tmp/test.pdf");
        when(reimbursementRepository.save(any())).thenAnswer(inv -> {
            Reimbursement r = inv.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });
        when(reimbursementMapper.toResponse(any())).thenAnswer(inv -> {
            Reimbursement r = inv.getArgument(0);
            ReimbursementDTO.Response dto = new ReimbursementDTO.Response();
            dto.setTauxRemboursement(r.getTauxRemboursement());
            dto.setMontantRembourse(r.getMontantRembourse());
            return dto;
        });

        ReimbursementDTO.Response response = reimbursementService.create(request, "127.0.0.1");
        assertThat(response.getTauxRemboursement()).isEqualTo(Constants.TAUX_GENERALISTE);
        assertThat(response.getMontantRembourse()).isEqualByComparingTo("100.00");
    }

    @Test
    void createReimbursement_specialiste_80Percent() throws Exception {
        record.getConsultation().setTypeConsultation(TypeConsultation.SPECIALISTE);
        ReimbursementDTO.CreateRequest request = new ReimbursementDTO.CreateRequest();
        request.setMedicalRecordId(recordId);
        request.setMontantTotal(new BigDecimal("200.00"));

        when(medicalRecordService.getEntity(recordId)).thenReturn(record);
        when(reimbursementRepository.existsByMedicalRecordId(recordId)).thenReturn(false);
        when(reimbursementRepository.countByNumPrefix(any())).thenReturn(5L);
        when(pdfGenerator.generateJustificatif(any())).thenReturn("/tmp/test2.pdf");
        when(reimbursementRepository.save(any())).thenAnswer(inv -> {
            Reimbursement r = inv.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });
        when(reimbursementMapper.toResponse(any())).thenAnswer(inv -> {
            Reimbursement r = inv.getArgument(0);
            ReimbursementDTO.Response dto = new ReimbursementDTO.Response();
            dto.setTauxRemboursement(r.getTauxRemboursement());
            dto.setMontantRembourse(r.getMontantRembourse());
            return dto;
        });

        ReimbursementDTO.Response response = reimbursementService.create(request, "127.0.0.1");
        assertThat(response.getTauxRemboursement()).isEqualTo(Constants.TAUX_SPECIALISTE);
        assertThat(response.getMontantRembourse()).isEqualByComparingTo("160.00");
    }

    @Test
    void dashboard_returnsStatistics() {
        when(reimbursementRepository.count()).thenReturn(10L);
        when(reimbursementRepository.countByStatus(ReimbursementStatus.PENDING)).thenReturn(3L);
        when(reimbursementRepository.countByStatus(ReimbursementStatus.APPROVED)).thenReturn(2L);
        when(reimbursementRepository.countByStatus(ReimbursementStatus.PAID)).thenReturn(4L);
        when(reimbursementRepository.countByStatus(ReimbursementStatus.REJECTED)).thenReturn(1L);
        when(reimbursementRepository.sumMontantPaye()).thenReturn(new BigDecimal("500.00"));
        when(reimbursementRepository.findByStatus(ReimbursementStatus.PAID)).thenReturn(List.of());

        ReimbursementDTO.DashboardResponse dashboard = reimbursementService.dashboard();

        assertThat(dashboard.getTotalRemboursements()).isEqualTo(10L);
        assertThat(dashboard.getPendingCount()).isEqualTo(3L);
        assertThat(dashboard.getPaidCount()).isEqualTo(4L);
        assertThat(dashboard.getMontantTotalPaye()).isEqualByComparingTo("500.00");
    }

    @Test
    void findAll_returnsPagedResults() {
        Reimbursement reimbursement = Reimbursement.builder()
                .id(UUID.randomUUID())
                .numRemboursement("REM-2026-000001")
                .medicalRecord(record)
                .montantTotal(new BigDecimal("100.00"))
                .tauxRemboursement(100)
                .montantRembourse(new BigDecimal("100.00"))
                .status(ReimbursementStatus.PENDING)
                .build();
        when(reimbursementRepository.findFiltered(any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(reimbursement)));
        when(reimbursementMapper.toResponse(reimbursement)).thenReturn(new ReimbursementDTO.Response());

        ReimbursementDTO.PageResponse response = reimbursementService.findAll(0, 20, null, null, null, null);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getStatistics().getNombreRemboursements()).isEqualTo(1);
    }

    @Test
    void findById_checksOwnership() {
        UUID reimbId = UUID.randomUUID();
        Reimbursement reimbursement = Reimbursement.builder()
                .id(reimbId)
                .numRemboursement("REM-2026-000002")
                .medicalRecord(record)
                .montantTotal(new BigDecimal("50.00"))
                .tauxRemboursement(100)
                .montantRembourse(new BigDecimal("50.00"))
                .status(ReimbursementStatus.PENDING)
                .build();
        ReimbursementDTO.Response dto = new ReimbursementDTO.Response();
        dto.setId(reimbId);

        when(reimbursementRepository.findById(reimbId)).thenReturn(Optional.of(reimbursement));
        doNothing().when(ownershipService).assertCanAccessReimbursement(reimbursement);
        when(reimbursementMapper.toResponse(reimbursement)).thenReturn(dto);

        ReimbursementDTO.Response result = reimbursementService.findById(reimbId);

        assertThat(result.getId()).isEqualTo(reimbId);
        verify(ownershipService).assertCanAccessReimbursement(reimbursement);
    }

    @Test
    void findForCurrentPatient_returnsOwnReimbursements() {
        Assure assure = new Assure();
        assure.setId(UUID.randomUUID());
        when(ownershipService.getCurrentAssure()).thenReturn(assure);
        when(reimbursementRepository.findByAssureId(eq(assure.getId()), any()))
                .thenReturn(new PageImpl<>(List.of()));
        assertThat(reimbursementService.findForCurrentPatient(0, 20).getContent()).isEmpty();
    }
}
