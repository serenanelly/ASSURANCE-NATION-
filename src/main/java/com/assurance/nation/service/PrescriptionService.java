package com.assurance.nation.service;

import com.assurance.nation.dto.PrescriptionDTO;
import com.assurance.nation.entity.*;
import com.assurance.nation.entity.enums.AuditAction;
import com.assurance.nation.exception.ResourceNotFoundException;
import com.assurance.nation.exception.ValidationException;
import com.assurance.nation.mapper.PrescriptionMapper;
import com.assurance.nation.repository.PrescriptionRepository;
import com.assurance.nation.security.OwnershipService;
import com.assurance.nation.security.SecurityUtil;
import com.assurance.nation.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final ConsultationService consultationService;
    private final MedecinService medecinService;
    private final PrescriptionMapper prescriptionMapper;
    private final AuditService auditService;
    private final OwnershipService ownershipService;
    private final NotificationService notificationService;

    @Transactional
    public PrescriptionDTO.Response create(UUID consultationId, PrescriptionDTO.CreateRequest request, String ip) {
        Consultation consultation = consultationService.getEntity(consultationId);
        ownershipService.assertCanAccessConsultation(consultation);
        Prescription prescription = switch (request.getType()) {
            case MEDICAMENT -> buildMedicament(consultation, request);
            case CONSULTATION_SPECIALISTE -> buildConsultationSpec(consultation, request);
        };
        prescription = prescriptionRepository.save(prescription);
        notificationService.notifyPrescriptionAdded(prescription);
        auditService.log("Prescription", prescription.getId().toString(), AuditAction.CREATE,
                auditService.findActorByEmail(SecurityUtil.getCurrentUserEmail()), null,
                prescriptionMapper.toResponse(prescription), ip);
        return prescriptionMapper.toResponse(prescription);
    }

    private PrescriptionMedicament buildMedicament(Consultation consultation, PrescriptionDTO.CreateRequest request) {
        if (request.getMedicament() == null || request.getMedicament().isBlank()) {
            throw new ValidationException("Nom du médicament obligatoire");
        }
        ValidationUtil.validateDuree(request.getDuree());
        PrescriptionMedicament pm = new PrescriptionMedicament();
        pm.setConsultation(consultation);
        pm.setMedicament(request.getMedicament());
        pm.setPosologie(request.getPosologie());
        pm.setDuree(request.getDuree());
        pm.setNotes(request.getNotes());
        return pm;
    }

    private PrescriptionConsultation buildConsultationSpec(Consultation consultation, PrescriptionDTO.CreateRequest request) {
        PrescriptionConsultation pc = new PrescriptionConsultation();
        pc.setConsultation(consultation);
        pc.setMotif(request.getMotif());
        pc.setPriorite(request.getPriorite());
        pc.setCodeReference(request.getCodeReference() != null
                ? request.getCodeReference()
                : generateCodeReference());
        if (request.getMedecinSpecialisteId() != null) {
            pc.setMedecinSpecialiste(medecinService.getEntity(request.getMedecinSpecialisteId()));
        }
        return pc;
    }

    private String generateCodeReference() {
        return "REF-" + System.currentTimeMillis();
    }

    @Transactional(readOnly = true)
    public PrescriptionDTO.Response findById(UUID id) {
        Prescription p = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription introuvable"));
        ownershipService.assertCanAccessConsultation(p.getConsultation());
        return prescriptionMapper.toResponse(p);
    }

    @Transactional(readOnly = true)
    public List<PrescriptionDTO.Response> findByConsultationId(UUID consultationId) {
        Consultation consultation = consultationService.getEntity(consultationId);
        ownershipService.assertCanAccessConsultation(consultation);
        return prescriptionRepository.findByConsultationId(consultationId)
                .stream().map(prescriptionMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PrescriptionDTO.Response> findAll(int page, int size) {
        return prescriptionRepository.findAll(PageRequest.of(page, Math.min(size, 100)))
                .getContent().stream().map(prescriptionMapper::toResponse).toList();
    }

    @Transactional
    public void delete(UUID id, String ip) {
        delete(id, null, ip);
    }

    @Transactional
    public void delete(UUID id, String motif, String ip) {
        Prescription p = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription introuvable"));
        ownershipService.assertCanAccessConsultation(p.getConsultation());
        p.softDelete();
        prescriptionRepository.save(p);
        auditService.log("Prescription", id.toString(), AuditAction.DELETE,
                auditService.findActorByEmail(SecurityUtil.getCurrentUserEmail()),
                motif, null, ip);
    }
}
