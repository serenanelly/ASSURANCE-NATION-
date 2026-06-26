package com.assurance.nation.mapper;

import com.assurance.nation.dto.ReimbursementDTO;
import com.assurance.nation.entity.Reimbursement;
import org.springframework.stereotype.Component;

@Component
public class ReimbursementMapper {

    public ReimbursementDTO.Response toResponse(Reimbursement r) {
        ReimbursementDTO.Response dto = new ReimbursementDTO.Response();
        dto.setId(r.getId());
        dto.setNumRemboursement(r.getNumRemboursement());
        dto.setMedicalRecordId(r.getMedicalRecord().getId());
        if (r.getMedicalRecord().getAssure() != null) {
            dto.setAssureId(r.getMedicalRecord().getAssure().getId());
            dto.setAssureNom(r.getMedicalRecord().getAssure().getNom());
        }
        dto.setMontantTotal(r.getMontantTotal());
        dto.setTauxRemboursement(r.getTauxRemboursement());
        dto.setMontantRembourse(r.getMontantRembourse());
        dto.setModePaiement(r.getModePaiement());
        dto.setStatus(r.getStatus());
        dto.setDateRemboursement(r.getDateRemboursement());
        dto.setCreatedAt(r.getCreatedAt());
        dto.setNotes(r.getNotes());
        if (r.getJustificatifPath() != null) {
            dto.setJustificatifUrl("/api/v1/reimbursements/" + r.getId() + "/justificatif");
        }
        return dto;
    }
}
