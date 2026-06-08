package com.assurance.nation.service;

import com.assurance.nation.dto.ConsultationDTO;
import com.assurance.nation.entity.Assure;
import com.assurance.nation.entity.Consultation;
import com.assurance.nation.entity.Medecin;
import com.assurance.nation.entity.MedicalRecord;
import com.assurance.nation.entity.enums.AuditAction;
import com.assurance.nation.exception.ResourceNotFoundException;
import com.assurance.nation.exception.UnauthorizedException;
import com.assurance.nation.mapper.ConsultationMapper;
import com.assurance.nation.repository.ConsultationRepository;
import com.assurance.nation.repository.MedicalRecordRepository;
import com.assurance.nation.repository.UserRepository;
import com.assurance.nation.security.OwnershipService;
import com.assurance.nation.security.RoleChecker;
import com.assurance.nation.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final AssureService assureService;
    private final MedecinService medecinService;
    private final UserRepository userRepository;
    private final ConsultationMapper consultationMapper;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final OwnershipService ownershipService;
    private final RoleChecker roleChecker;

    @Transactional
    public ConsultationDTO.Response create(ConsultationDTO.CreateRequest request, String ip) {
        Medecin medecin = resolveCurrentMedecin();
        Assure assure = assureService.getEntity(request.getAssureId());
        Consultation consultation = Consultation.builder()
                .assure(assure)
                .medecin(medecin)
                .dateConsultation(request.getDateConsultation())
                .typeConsultation(request.getTypeConsultation())
                .diagnostique(request.getDiagnostique())
                .motif(request.getMotif())
                .notes(request.getNotes())
                .build();
        consultation = consultationRepository.save(consultation);
        MedicalRecord record = MedicalRecord.builder()
                .assure(assure)
                .medecin(medecin)
                .consultation(consultation)
                .date(LocalDate.now())
                .nomMaladie(request.getDiagnostique())
                .estRemboursee(false)
                .build();
        medicalRecordRepository.save(record);
        consultation.setMedicalRecord(record);
        notificationService.notifyConsultationCreated(consultation);
        auditService.log("Consultation", consultation.getId().toString(), AuditAction.CREATE,
                auditService.findActorByEmail(SecurityUtil.getCurrentUserEmail()), null,
                consultationMapper.toResponse(consultation), ip);
        return consultationMapper.toResponse(consultation);
    }

    @Transactional(readOnly = true)
    public List<ConsultationDTO.Response> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<Consultation> result;
        if (roleChecker.isMedecin()) {
            Medecin medecin = ownershipService.getCurrentMedecin();
            result = consultationRepository.findByMedecinId(medecin.getId(), pageable);
        } else if (roleChecker.isPatient()) {
            Assure assure = ownershipService.getCurrentAssure();
            result = consultationRepository.findByAssureId(assure.getId(), pageable);
        } else {
            result = consultationRepository.findAll(pageable);
        }
        return result.getContent().stream().map(consultationMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ConsultationDTO.Response findById(UUID id) {
        Consultation c = consultationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation introuvable"));
        ownershipService.assertCanAccessConsultation(c);
        return consultationMapper.toResponse(c);
    }

    @Transactional
    public ConsultationDTO.Response update(UUID id, ConsultationDTO.UpdateRequest request, String ip) {
        Consultation c = consultationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation introuvable"));
        ownershipService.assertCanAccessConsultation(c);
        boolean rescheduled = Boolean.TRUE.equals(request.getReschedule());
        if (request.getDateConsultation() != null) c.setDateConsultation(request.getDateConsultation());
        if (request.getTypeConsultation() != null) c.setTypeConsultation(request.getTypeConsultation());
        if (request.getDiagnostique() != null) c.setDiagnostique(request.getDiagnostique());
        if (request.getMotif() != null) c.setMotif(request.getMotif());
        if (request.getNotes() != null) c.setNotes(request.getNotes());
        c = consultationRepository.save(c);
        if (rescheduled) {
            notificationService.notifyConsultationRescheduled(c);
        }
        auditService.log("Consultation", id.toString(), AuditAction.UPDATE,
                auditService.findActorByEmail(SecurityUtil.getCurrentUserEmail()), null,
                consultationMapper.toResponse(c), ip);
        return consultationMapper.toResponse(c);
    }

    @Transactional
    public void cancel(UUID id, ConsultationDTO.CancelRequest request, String ip) {
        Consultation c = consultationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation introuvable"));
        ownershipService.assertCanAccessConsultation(c);
        if (request != null && request.getMotifAnnulation() != null) {
            c.setNotes(request.getMotifAnnulation());
        }
        c.softDelete();
        consultationRepository.save(c);
        if (request == null || !Boolean.FALSE.equals(request.getNotifyPatient())) {
            notificationService.notifyConsultationCancelled(c);
        }
        auditService.log("Consultation", id.toString(), AuditAction.DELETE,
                auditService.findActorByEmail(SecurityUtil.getCurrentUserEmail()), null, null, ip);
    }

    @Transactional(readOnly = true)
    public Consultation getEntity(UUID id) {
        return consultationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation introuvable"));
    }

    private Medecin resolveCurrentMedecin() {
        return userRepository.findByEmail(SecurityUtil.getCurrentUserEmail())
                .filter(Medecin.class::isInstance)
                .map(Medecin.class::cast)
                .orElseThrow(() -> new UnauthorizedException("Seul un médecin peut effectuer cette action"));
    }
}
