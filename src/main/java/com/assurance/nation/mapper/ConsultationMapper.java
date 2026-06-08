package com.assurance.nation.mapper;

import com.assurance.nation.dto.ConsultationDTO;
import com.assurance.nation.entity.Consultation;
import org.springframework.stereotype.Component;

@Component
public class ConsultationMapper {

    public ConsultationDTO.Response toResponse(Consultation c) {
        ConsultationDTO.Response dto = new ConsultationDTO.Response();
        dto.setId(c.getId());
        dto.setAssureId(c.getAssure().getId());
        dto.setAssureNom(c.getAssure().getNom() + " " + c.getAssure().getPrenom());
        dto.setMedecinId(c.getMedecin().getId());
        dto.setMedecinNom(c.getMedecin().getNom() + " " + c.getMedecin().getPrenom());
        dto.setDateConsultation(c.getDateConsultation());
        dto.setTypeConsultation(c.getTypeConsultation());
        dto.setDiagnostique(c.getDiagnostique());
        dto.setMotif(c.getMotif());
        dto.setNotes(c.getNotes());
        if (c.getMedicalRecord() != null) {
            dto.setMedicalRecordId(c.getMedicalRecord().getId());
        }
        return dto;
    }
}
