package com.assurance.nation.service;

import com.assurance.nation.dto.MedicalRecordDTO;
import com.assurance.nation.dto.PageDTO;
import com.assurance.nation.entity.MedicalRecord;
import com.assurance.nation.exception.ResourceNotFoundException;
import com.assurance.nation.repository.MedicalRecordRepository;
import com.assurance.nation.security.OwnershipService;
import com.assurance.nation.util.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final OwnershipService ownershipService;

    @Transactional(readOnly = true)
    public PageDTO<MedicalRecordDTO.Response> findByAssureId(
            UUID assureId, int page, int size,
            LocalDate startDate, LocalDate endDate, String status) {
        ownershipService.assertCanAccessAssure(assureId);
        Boolean remboursee = parseStatus(status);
        PageRequest pageable = PageRequest.of(page, Math.min(size, Constants.MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "date"));
        Page<MedicalRecord> result = medicalRecordRepository.findByAssureFiltered(
                assureId, startDate, endDate, remboursee, pageable);
        return PageDTO.of(result, result.getContent().stream().map(this::toResponse).toList());
    }

    private Boolean parseStatus(String status) {
        if (status == null || status.isBlank()) return null;
        return "REMBOURSEE".equalsIgnoreCase(status);
    }

    @Transactional(readOnly = true)
    public MedicalRecordDTO.Response findById(UUID id) {
        MedicalRecord record = getEntity(id);
        ownershipService.assertCanAccessMedicalRecord(record);
        return toResponse(record);
    }

    @Transactional
    public MedicalRecordDTO.Response update(UUID id, MedicalRecordDTO.UpdateRequest request) {
        MedicalRecord record = getEntity(id);
        ownershipService.assertCanAccessMedicalRecord(record);
        if (record.isEstRemboursee()) {
            throw new com.assurance.nation.exception.BusinessException(
                    "Feuille de maladie non modifiable après remboursement");
        }
        record.setNomMaladie(request.getNomMaladie());
        if (request.getDate() != null) {
            record.setDate(request.getDate());
        }
        record = medicalRecordRepository.save(record);
        return toResponse(record);
    }

    @Transactional(readOnly = true)
    public MedicalRecord getEntity(UUID id) {
        return medicalRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feuille de maladie introuvable"));
    }

    private MedicalRecordDTO.Response toResponse(MedicalRecord r) {
        MedicalRecordDTO.Response dto = new MedicalRecordDTO.Response();
        dto.setId(r.getId());
        dto.setAssureId(r.getAssure().getId());
        dto.setMedecinId(r.getMedecin().getId());
        dto.setConsultationId(r.getConsultation().getId());
        dto.setDate(r.getDate());
        dto.setNomMaladie(r.getNomMaladie());
        dto.setEstRemboursee(r.isEstRemboursee());
        dto.setDateRemboursement(r.getDateRemboursement());
        dto.setMontantRembourse(r.getMontantRembourse());
        return dto;
    }
}
