package com.assurance.nation.service;

import com.assurance.nation.entity.Assure;
import com.assurance.nation.entity.Consultation;
import com.assurance.nation.entity.MedicalRecord;
import com.assurance.nation.entity.PrescriptionMedicament;
import com.assurance.nation.entity.Reimbursement;
import com.assurance.nation.entity.enums.ReimbursementStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private EmailService emailService;
    @InjectMocks private NotificationService notificationService;

    @Test
    void notifyConsultationCreated_sendsEmail() {
        Assure assure = new Assure();
        assure.setEmail("p@test.com");
        Consultation c = Consultation.builder()
                .assure(assure)
                .dateConsultation(LocalDateTime.now())
                .build();
        notificationService.notifyConsultationCreated(c);
        verify(emailService).sendAsync(eq("p@test.com"), contains("consultation"), anyString());
    }

    @Test
    void notifyConsultationCancelled_sendsEmail() {
        Assure assure = new Assure();
        assure.setEmail("p@test.com");
        Consultation c = Consultation.builder().assure(assure).dateConsultation(LocalDateTime.now()).build();
        notificationService.notifyConsultationCancelled(c);
        verify(emailService).sendAsync(eq("p@test.com"), contains("annulée"), anyString());
    }

    @Test
    void notifyPrescriptionAdded_sendsEmail() {
        Assure assure = new Assure();
        assure.setEmail("p@test.com");
        Consultation c = Consultation.builder().assure(assure).dateConsultation(LocalDateTime.now()).build();
        PrescriptionMedicament p = new PrescriptionMedicament();
        p.setConsultation(c);
        notificationService.notifyPrescriptionAdded(p);
        verify(emailService).sendAsync(eq("p@test.com"), contains("prescription"), anyString());
    }

    @Test
    void notifyReimbursementApproved_sendsEmail() {
        Assure assure = new Assure();
        assure.setEmail("p@test.com");
        MedicalRecord record = MedicalRecord.builder().assure(assure).build();
        Reimbursement r = Reimbursement.builder()
                .numRemboursement("RB-2026-000001")
                .medicalRecord(record)
                .montantRembourse(new BigDecimal("50"))
                .status(ReimbursementStatus.APPROVED)
                .build();
        notificationService.notifyReimbursementApproved(r, assure);
        verify(emailService).sendAsync(eq("p@test.com"), contains("approuvé"), anyString());
    }

    @Test
    void notifyReimbursementRejected_sendsEmail() {
        Assure assure = new Assure();
        assure.setEmail("p@test.com");
        MedicalRecord record = MedicalRecord.builder().assure(assure).build();
        Reimbursement r = Reimbursement.builder()
                .numRemboursement("RB-2026-000002")
                .medicalRecord(record)
                .build();
        notificationService.notifyReimbursementRejected(r, "Dossier incomplet");
        verify(emailService).sendAsync(eq("p@test.com"), contains("refusé"), anyString());
    }

    @Test
    void notifyReimbursementPaid_sendsEmail() {
        Assure assure = new Assure();
        assure.setEmail("p@test.com");
        Reimbursement r = Reimbursement.builder()
                .numRemboursement("RB-2026-000003")
                .montantRembourse(new BigDecimal("30"))
                .build();
        notificationService.notifyReimbursementPaid(r, assure);
        verify(emailService).sendAsync(eq("p@test.com"), contains("payé"), anyString());
    }
}
