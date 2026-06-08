package com.assurance.nation.service;

import com.assurance.nation.entity.Assure;
import com.assurance.nation.entity.Consultation;
import com.assurance.nation.entity.Prescription;
import com.assurance.nation.entity.Reimbursement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Notifications par email pour les événements métier.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailService emailService;

    public void notifyConsultationCreated(Consultation consultation) {
        Assure assure = consultation.getAssure();
        emailService.sendAsync(
                assure.getEmail(),
                "Nouvelle consultation - ASSURANCE NATION",
                "Une consultation a été enregistrée le " + consultation.getDateConsultation());
    }

    public void notifyConsultationCancelled(Consultation consultation) {
        Assure assure = consultation.getAssure();
        emailService.sendAsync(
                assure.getEmail(),
                "Consultation annulée - ASSURANCE NATION",
                "Votre consultation du " + consultation.getDateConsultation() + " a été annulée.");
    }

    public void notifyConsultationRescheduled(Consultation consultation) {
        Assure assure = consultation.getAssure();
        emailService.sendAsync(
                assure.getEmail(),
                "Consultation reportée - ASSURANCE NATION",
                "Votre consultation a été reportée au " + consultation.getDateConsultation());
    }

    public void notifyReimbursementCreated(Reimbursement reimbursement, Assure assure) {
        emailService.sendAsync(
                assure.getEmail(),
                "Demande de remboursement - ASSURANCE NATION",
                "Votre demande " + reimbursement.getNumRemboursement()
                        + " d'un montant de " + reimbursement.getMontantRembourse() + " est en cours de traitement.");
    }

    public void notifyReimbursementApproved(Reimbursement reimbursement, Assure assure) {
        emailService.sendAsync(
                assure.getEmail(),
                "Remboursement approuvé - ASSURANCE NATION",
                "Votre remboursement " + reimbursement.getNumRemboursement()
                        + " a été approuvé. Montant : " + reimbursement.getMontantRembourse() + " €.");
    }

    public void notifyReimbursementRejected(Reimbursement reimbursement, String motif) {
        Assure assure = reimbursement.getMedicalRecord().getAssure();
        emailService.sendAsync(
                assure.getEmail(),
                "Remboursement refusé - ASSURANCE NATION",
                "Votre remboursement " + reimbursement.getNumRemboursement()
                        + " a été refusé. Motif : " + motif);
    }

    public void notifyPrescriptionAdded(Prescription prescription) {
        Assure assure = prescription.getConsultation().getAssure();
        emailService.sendAsync(
                assure.getEmail(),
                "Nouvelle prescription - ASSURANCE NATION",
                "Une prescription a été ajoutée à votre consultation du "
                        + prescription.getConsultation().getDateConsultation());
    }

    public void notifyReimbursementPaid(Reimbursement reimbursement, Assure assure) {
        emailService.sendAsync(
                assure.getEmail(),
                "Remboursement payé - ASSURANCE NATION",
                "Votre remboursement " + reimbursement.getNumRemboursement()
                        + " de " + reimbursement.getMontantRembourse() + " € a été versé.");
    }
}
